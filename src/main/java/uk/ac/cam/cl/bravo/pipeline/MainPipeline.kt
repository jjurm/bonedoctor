package uk.ac.cam.cl.bravo.pipeline

import com.jhlabs.image.FlipFilter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import uk.ac.cam.cl.bravo.PLANE_SIZE
import uk.ac.cam.cl.bravo.classify.*
import uk.ac.cam.cl.bravo.dataset.*
import uk.ac.cam.cl.bravo.hash.FractureHighlighter2
import uk.ac.cam.cl.bravo.hash.FractureHighlighter2Impl
import uk.ac.cam.cl.bravo.hash.ImageMatcher
import uk.ac.cam.cl.bravo.hash.ImageMatcherImpl
import uk.ac.cam.cl.bravo.overlay.*
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI
import uk.ac.cam.cl.bravo.util.ImageTools
import uk.ac.cam.cl.bravo.util.combineObservables
import uk.ac.cam.cl.bravo.util.mapDestructing
import java.awt.image.BufferedImage
import java.io.File
import java.util.function.BiFunction
import javax.imageio.ImageIO

/**
 * Responsible: Juraj Micko (jm2186)
 */
class MainPipeline {

    val dataset = Dataset()

    /**
     * Normalises the given image to have the same size (520x520).
     */
    fun normaliseImage(image: BufferedImage) = ImageTools.copyToPlane(image, planeSize = PLANE_SIZE)

    // ===== SUBJECTS =====
    // Subjects can hold a value (an input, a parameter, etc.).
    // Change the value by calling subject.onNext(newValue)

    /**
     * In our algorithms, there is a trade-off between performance and precision. This argument specifies the precision
     * level (higher values take longer to compute). Values are 0.0 to 1.0
     */
    val precision: Subject<Double> = BehaviorSubject.createDefault(0.8)

    /**
     * A value between 0.0 and 1.0
     */
    val highlightAmount: Subject<Double> = BehaviorSubject.createDefault(0.0)

    /**
     * A value between 0.0 and 1.0
     */
    val highlightGradient: Subject<Double> = BehaviorSubject.createDefault(0.5)

    /** A pair of (input image path, bodypart of the input image) */
    val userInput: Subject<Pair<String, Bodypart>> = BehaviorSubject.create()

    /**
     * An image to overlay the user image with.
     *
     * As soon as the list of similar images is computed, the first one is put to this subject.
     * If the user chooses another ImageSample for overlay, call imageToOverlay.onNext(imageChosenByUser)
     */
    val imageToOverlay: Subject<ImageSample> = BehaviorSubject.create<ImageSample>()

    // ===== OBSERVABLES =====
    // One can subscribe to an observable to get updates/results from it,
    // by calling observable.subscribe(Consumer<...>)

    /** Progress of the pipeline. A value between 0.0 and 1.0 */
    val progress: Observable<Double>

    /** A status message to show to the user */
    val status: Observable<String>

    /** Input image from the user, preprocessed. */
    val preprocessed: Observable<Uncertain<BufferedImage>>

    /** Classified BoneCondition, taking the 'preprocessed' image as the input */
    val boneCondition: Observable<Uncertain<BoneCondition>>

    /** List of matched images (similar to 'preprocessed') that are NORMAL */
    val similarNormal: Observable<List<Rated<ImageSample>>>

    /** The result of the overlay algorithm, taking 'imageToOverlay' as the input. */
    val transformedAndOverlaid: Observable<Rated<Pair<BufferedImage, BufferedImage>>>

    /** Intermediate results of the overlay algorithm */
    val transformedAndOverlaidOriginal: Observable<Rated<Pair<BufferedImage, BufferedImage>>>
    val transformedAndOverlaidMirrored: Observable<Rated<Pair<BufferedImage, BufferedImage>>>

    /** Image highlighting the difference between the two overlaid images */
    val overlaidDifferences: Observable<BufferedImage>

    /** The transformed image modified to highlight differences from 'preprocessed' */
    val fracturesHighlighted: Observable<BufferedImage>


    // ===== Constants =====

    private val nSimilarImages = Observable.just(12)
    private val nPreciseSimilarImages = Observable.just(4)

    companion object {
        const val PROGRESS_PREPROCESSING = 0
    }

    // ===== COMPONENTS =====

    private val progress0: Subject<Double> = BehaviorSubject.createDefault(0.0)

    private val preprocessor: ImagePreprocessor = ImagePreprocessorI { progress0.onNext(it * 0.2) }
    private val boneConditionClassifier: BoneConditionClassifier = BoneConditionClassifierImpl()
    private val bodypartViewClassifier: BodypartViewClassifier = BodypartViewClassifierImpl()
    private val imageMatcher: ImageMatcher = ImageMatcherImpl.getImageMatcher(File(Dataset.IMAGE_MATCHER_FILE))
    private val preciseImageMatcher: PreciseImageMatcher = PreciseImageMatcherImpl()
    private val imageOverlay: ImageOverlay = ImageOverlayImpl(
        arrayOf(
            InnerWarpTransformer(
                parameterScale = 0.4,
                parameterPenaltyScale = 4.0,
                resolution = 4
            ),
            AffineTransformer(
                parameterScale = 1.0,
                parameterPenaltyScale = 1.0
            )
        ),
        PixelSimilarity(
            ignoreBorderWidth = 0.20
        ) + ParameterPenaltyFunction(power = 1.2) * 1.0,
        bigPlaneSize = PLANE_SIZE
    )
    private val overlayDifference: BiFunction<BufferedImage, BufferedImage, BufferedImage> = ImageDifference()
    private val fractureHighlighter: FractureHighlighter2 = FractureHighlighter2Impl()

    init {
        // === Subjects ===

        // progress0 has type Subject<Double> (so that we can call .onNext(...))
        // but the exposed 'progress' variable is just Observable
        progress = progress0
        val status0 = BehaviorSubject.createDefault("Program started")
        status = status0

        // === Pipeline ===

        /** When 'this' observable emits a value, the given progress and status are reported to the UI. */
        fun <T> Observable<T>.doneMeans(progress: Double, status: String? = null) {
            this.subscribe { progress0.onNext(progress) }
            if (status != null) {
                this.subscribe { status0.onNext(status) }
            }
        }

        // map [0.0, 1.0] to [3.0, 1.0]
        val downsample = precision.map { x -> Math.pow(x - 1, 2.0) * 2 + 1 }
        // map [0.0, 1.0] to [1e-3, 1e-5]
        val overlayPrecision = precision.map { x ->
            (Math.pow(10.0, -3 - 2 * x) + 1e-3 - x * (1e-3 - 1e-5)) / 2
        }

        userInput.doneMeans(0.0, "Pre-processing the input x-ray")

        val path = userInput.map(Pair<String, *>::first)
        val bodypart = userInput.map(Pair<*, Bodypart>::second)
        val inputImage = path
            .map { ImageIO.read(File(it)) }
            .observeOn(Schedulers.io())
            .withCache()

        preprocessed = path
            .observeOn(Schedulers.newThread())
            .map(preprocessor::preprocess.withTag("Preprocessing"))
            .withCache()
        preprocessed.doneMeans(0.2, "Classifying bone condition")

        val preprocessedVal = preprocessed.map(Uncertain<BufferedImage>::value)

        boneCondition = inputImage
            .observeOn(Schedulers.newThread())
            .map(boneConditionClassifier::classify.withTag("BoneCondition classifier"))
            .withCache()

        val bodypartView = combineObservables(inputImage, bodypart)
            .observeOn(Schedulers.newThread())
            .mapDestructing(bodypartViewClassifier::classify.withTag("BodypartView classifier"))
            .withCache()

        bodypartView.doneMeans(0.4, "Looking for similar x-rays")

        val bodypartViewVal = bodypartView.map(Uncertain<BodypartView>::value)
        bodypartViewVal.subscribe { println("Classified view: ${it.value}") }

        // Output of ImageMatcher contains Files; so convert it to ImageSamples, by looking up the path in the Dataset
        val matchingFunction =
            { image: BufferedImage, boneCondition: BoneCondition, bpView: BodypartView, n: Int ->
                imageMatcher.findMatchingImage(image, boneCondition, bpView, n, true).map {
                    try {
                        // throws exception if ImageSample not loaded in the dataset
                        val imageSample = dataset.combined.getValue(it.key.toString().replace('\\', '/'))
                        Rated(value = imageSample, score = it.value.toDouble())
                    } catch (e: NoSuchElementException) {
                        throw RuntimeException("Image ${it.key} returned by ImageMatcher is not in the dataset", e)
                    }
                }
            }.withTag("ImageMatcher")

        // TODO Juraj: choose original or preprocessed image for ImageMatcher
        val intermediateSimilarNormal =
            combineObservables(inputImage, Observable.just(BoneCondition.NORMAL), bodypartViewVal, nSimilarImages)
                .observeOn(Schedulers.newThread())
                .mapDestructing(matchingFunction)
                .withCache()

        similarNormal = combineObservables(
            inputImage,
            intermediateSimilarNormal.map { it.map { it.value } },
            nPreciseSimilarImages
        )
            .observeOn(Schedulers.newThread())
            .mapDestructing(preciseImageMatcher::findMatchingImages.withTag("PreciseImageMatcher"))
            .withCache()

        similarNormal.map { it.get(0).value }.subscribe(imageToOverlay)
        imageToOverlay.doneMeans(0.6, "Overlaying images")

        val imageToOverlayLoaded = imageToOverlay
            .observeOn(Schedulers.io())
            .map(ImageSample::loadPreprocessedImage)
            .withCache()

        val imageToOverlayLoadedMirrored = imageToOverlayLoaded
            .observeOn(Schedulers.newThread())
            .map { FlipFilter(FlipFilter.FLIP_H).filter(it, null) }
            .withCache()

        transformedAndOverlaidOriginal = combineObservables(
            preprocessedVal,
            bodypartViewVal,
            imageToOverlayLoaded,
            downsample,
            overlayPrecision
        )
            .observeOn(Schedulers.newThread())
            .mapDestructing(imageOverlay::fitImage.withTag("OverlayOriginal"))
            .withCache()

        transformedAndOverlaidMirrored = combineObservables(
            preprocessedVal,
            bodypartViewVal,
            imageToOverlayLoadedMirrored,
            downsample,
            overlayPrecision
        )
            .observeOn(Schedulers.newThread())
            .mapDestructing(imageOverlay::fitImage.withTag("OverlayMirrored"))
            .withCache()

        transformedAndOverlaid = combineObservables(transformedAndOverlaidOriginal, transformedAndOverlaidMirrored)
            .mapDestructing { a, b -> listOf(a, b).minBy { it.score }!! }
        transformedAndOverlaid.doneMeans(0.8, "Highlighting differences")

        val preprocessedNormalised = preprocessedVal
            .observeOn(Schedulers.computation())
            .map(imageOverlay::normalise)
            .withCache()

        overlaidDifferences = combineObservables(
            preprocessedNormalised,
            transformedAndOverlaid.map { it.value.first }
        )
            .observeOn(Schedulers.computation())
            .mapDestructing(overlayDifference::apply)
            .withCache()

        combineObservables(
            inputImage, imageToOverlayLoaded, highlightAmount, highlightGradient
        )
            .observeOn(Schedulers.newThread())
            .mapDestructing(fractureHighlighter::highlight.withTag("FractureHighlighter"))
            .subscribe()

        fracturesHighlighted = fractureHighlighter.partialResults

        val done = combineObservables(transformedAndOverlaid, fracturesHighlighted, boneCondition).map { Unit }
        done.doneMeans(1.0, "Done!")
    }

    /**
     * This passively mediates many subscriptions by using only a single subscription to the receiver Observable.
     * Passively because 'subscribe' calls to the returned Observable do not propagate back to the receiver Observable.
     */
    private fun <T> Observable<T>.withCache(): Observable<T> {
        val subject = BehaviorSubject.create<T>()
        subscribe(subject)
        return subject
    }

    // --- Functions for easier debugging ---

    fun <R> tag(tag: String, f: () -> R): R {
        println("STARTED:  $tag")
        val r = f()
        println("FINISHED: $tag")
        return r
    }

    private fun <A, R> ((A) -> R).withTag(s: String): (A) -> R = { tag(s) { this(it) } }
    private fun <A, B, R> ((A, B) -> R).withTag(s: String): (A, B) -> R = { a, b -> tag(s) { this(a, b) } }
    private fun <A, B, C, R> ((A, B, C) -> R).withTag(s: String): (A, B, C) -> R =
        { a, b, c -> tag(s) { this(a, b, c) } }

    private fun <A, B, C, D, R> ((A, B, C, D) -> R).withTag(s: String): (A, B, C, D) -> R =
        { a, b, c, d -> tag(s) { this(a, b, c, d) } }

    private fun <A, B, C, D, E, R> ((A, B, C, D, E) -> R).withTag(s: String): (A, B, C, D, E) -> R =
        { a, b, c, d, e -> tag(s) { this(a, b, c, d, e) } }

}
