package uk.ac.cam.cl.bravo.pipeline

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import uk.ac.cam.cl.bravo.PLANE_SIZE
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifier
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifierImpl
import uk.ac.cam.cl.bravo.classify.BoneConditionClassifier
import uk.ac.cam.cl.bravo.classify.BoneConditionClassifierImpl
import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.BodypartView
import uk.ac.cam.cl.bravo.dataset.BoneCondition
import uk.ac.cam.cl.bravo.dataset.ImageSample
import uk.ac.cam.cl.bravo.hash.ImageMatcher
import uk.ac.cam.cl.bravo.overlay.*
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI
import java.awt.image.BufferedImage

/**
 * Responsible: Juraj Micko (jm2186)
 */
class MainPipeline {

    // ===== SUBJECTS =====
    // Subjects can hold a value (an input, a parameter, etc.).
    // Change the value by calling subject.onNext(newValue)

    /**
     * In our algorithms, there is a trade-off between performance and precision. This argument specifies the precision
     * level (higher values take longer to compute). Values are 0.0 to 1.0
     *
     * TODO Juraj: use this in the pipeline
     */
    val precision: Subject<Double> = BehaviorSubject.createDefault(0.5)

    /** A pair of (input image, bodypart of the input image) */
    val userInput: Subject<Pair<String, Bodypart>>

    /**
     * An image to overlay the user image with.
     *
     * As soon as the list of similar images is computed, the first one is put to this subject.
     * If the user chooses another ImageSample for overlay, call imageToOverlay.onNext(imageChosenByUser)
     */
    val imageToOverlay = BehaviorSubject.create<ImageSample>()

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
    /** List of matched images (similar to 'preprocessed') that are ABNORMAL */
    val similarAbnormal: Observable<List<Rated<ImageSample>>>

    /** The result of the overlay algorithm, taking 'imageToOverlay' as the input. */
    val overlayed: Observable<Rated<BufferedImage>>

    /** The 'overlayed' image modified to highlight differences from 'preprocessed' */
    val preprocessedHighlighted: Observable<BufferedImage>


    // ===== Constants =====

    private val nSimilarImages = Observable.just(5)

    companion object {
        const val PROGRESS_PREPROCESSING = 0
    }

    // ===== COMPONENTS =====

    private val preprocessor: ImagePreprocessor = ImagePreprocessorI({
        // TODO Juraj: handle progress updates
    })
    private val boneConditionClassifier: BoneConditionClassifier = BoneConditionClassifierImpl()
    private val bodypartViewClassifier: BodypartViewClassifier = BodypartViewClassifierImpl()
    lateinit private var imageMatcher: ImageMatcher // TODO Shehab: instantiate ImageMatcher
    private val imageOverlay: ImageOverlay = ImageOverlayImpl(
        arrayOf(
            InnerWarpTransformer(
                parameterScale = 0.8,
                parameterPenaltyScale = 1.0,
                resolution = 4
            ),
            AffineTransformer(
                parameterScale = 1.0,
                parameterPenaltyScale = 0.1
            )
        ),
        PixelSimilarity(
            ignoreBorderWidth = 0.25
        ) + ParameterPenaltyFunction() * 0.05,
        bigPlaneSize = PLANE_SIZE,
        downsample = 1.0,
        precision = 1e-5
    )

    init {
        // progress0 has type BehaviorSubject<Double> (so that we can call .onNext(...))
        // but the exposed 'progress' variable is just Observable
        val progress0 = BehaviorSubject.createDefault(0.0)
        progress = progress0
        val status0 = BehaviorSubject.createDefault("Program started")
        status = status0

        /** When 'this' observable emits a value, the given progress and status are reported to the UI. */
        fun <T> Observable<T>.doneMeans(progress: Double, status: String? = null) {
            this.subscribe { progress0.onNext(progress) }
            if (status != null) {
                this.subscribe { status0.onNext(status) }
            }
        }

        userInput = BehaviorSubject.create()
        userInput.doneMeans(0.0, "Pre-processing the input x-ray")

        val path = userInput.map(Pair<String, *>::first)
        val bodypart = userInput.map(Pair<*, Bodypart>::second)

        preprocessed = path.map(preprocessor::preprocess)
        preprocessed.doneMeans(0.2, "Classifying bone condition")
        val preprocessedVal = preprocessed.map(Uncertain<BufferedImage>::value)

        boneCondition = preprocessedVal.map(boneConditionClassifier::classify)

        val bodypartView: Observable<Uncertain<BodypartView>> = Observable.combineLatest(
            preprocessedVal,
            bodypart,
            BiFunction(bodypartViewClassifier::classify)
        )
        bodypartView.doneMeans(0.4, "Looking for similar x-rays")
        val bodypartViewVal = bodypartView.map(Uncertain<BodypartView>::value)

        similarNormal = Observable.combineLatest(
            preprocessedVal, Observable.just(BoneCondition.NORMAL), bodypartViewVal, nSimilarImages,
            Function4(imageMatcher::findMatchingImage)
        )
        similarAbnormal = Observable.combineLatest(
            preprocessedVal, Observable.just(BoneCondition.ABNORMAL), bodypartViewVal, nSimilarImages,
            Function4(imageMatcher::findMatchingImage)
        )

        similarNormal.map { it.first().value }.subscribe(imageToOverlay)
        imageToOverlay.doneMeans(0.6, "Overlaying images")

        overlayed = Observable.combineLatest(
            preprocessedVal,
            imageToOverlay.map(ImageSample::loadPreprocessedImage),
            BiFunction(imageOverlay::fitImage)
        )
        overlayed.doneMeans(0.8, "Highlighting differences")

        // TODO Shehab: highlight differences
        preprocessedHighlighted = overlayed.map { it.value }
        preprocessedHighlighted.doneMeans(1.0, "Done!")
    }

}
