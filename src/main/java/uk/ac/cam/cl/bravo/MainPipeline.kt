package uk.ac.cam.cl.bravo

import uk.ac.cam.cl.bravo.classify.BodypartViewClassifier
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifierImpl
import uk.ac.cam.cl.bravo.classify.BoneConditionClassifier
import uk.ac.cam.cl.bravo.classify.BoneConditionClassifierImpl
import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.BoneCondition
import uk.ac.cam.cl.bravo.hash.ImageMatcher
import uk.ac.cam.cl.bravo.overlay.*
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI
import java.util.concurrent.Executors

class MainPipeline(private val observer: MainPipelineObserver) {

    private val executor = Executors.newFixedThreadPool(2)

    val preprocessor: ImagePreprocessor = ImagePreprocessorI()
    val boneConditionClassifier: BoneConditionClassifier = BoneConditionClassifierImpl()
    val bodypartViewClassifier: BodypartViewClassifier = BodypartViewClassifierImpl()
    lateinit var imageMatcher: ImageMatcher // TODO
    val imageOverlay: ImageOverlay = ImageOverlayImpl(
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


    fun submit(path: String, bodypart: Bodypart) {

        // Pre-processing

        observer.statusUpdate("Pre-processing the input image")
        observer.overallProgress(0.0)
        val preprocessed = preprocessor.preprocess(path)
        val userImage = imageOverlay.normalise(preprocessed)
        observer.overallProgress(0.2)
        observer.preprocessedUserImage(userImage)

        // Classify BoneCondition & BodypartView (in parallel)

        observer.statusUpdate("Classifying bone condition and x-ray view")
        executor.submit {
            val boneCondition = boneConditionClassifier.classify(preprocessed)
            observer.reportBoneCondition(boneCondition)
        }

        val bodypartView = bodypartViewClassifier.classify(preprocessed, bodypart)
        observer.overallProgress(0.4)

        // Find similar image

        observer.statusUpdate("Looking for similar x-rays")
        val similarNormal = imageMatcher.findMatchingImage(preprocessed, BoneCondition.NORMAL, bodypartView)
        val similarAbnormal = imageMatcher.findMatchingImage(preprocessed, BoneCondition.ABNORMAL, bodypartView)
        observer.overallProgress(0.6)

        // Overlay

        observer.statusUpdate("Overlaying x-rays with the input")
        val overlayNormal = imageOverlay.fitImage(preprocessed, similarNormal.loadPreprocessedImage())
        observer.overallProgress(1.0)

        // Done!

        observer.statusUpdate("Done")
        observer.success(overlayNormal, null)
    }

}
