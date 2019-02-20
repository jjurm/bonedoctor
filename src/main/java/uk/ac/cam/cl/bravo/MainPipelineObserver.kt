package uk.ac.cam.cl.bravo

import uk.ac.cam.cl.bravo.dataset.BoneCondition
import java.awt.image.BufferedImage

/**
 * The UI should supply an object implementing this interface to MainPipeline, to get updates
 */
interface MainPipelineObserver {

    /**
     * Notifies the observer about the progress of the pipeline.
     * @param progress a value between 0.0 and 1.0
     */
    fun overallProgress(progress: Double)

    /**
     * Let the user know what the program is doing.
     * The UI should display the last message.
     */
    fun statusUpdate(message: String)

    /**
     * Called when the BoneConditionClassifier is done
     */
    fun reportBoneCondition(boneCondition: BoneCondition)

    /**
     * This is called when the input image from the user has been pre-processed.
     */
    fun preprocessedUserImage(image: BufferedImage)

    /**
     * Called on partial results from the overlaying algorithm. The overlay may be inaccurate.
     */
    fun partialOverlay(matchedNormal: BufferedImage?, matchedAbnormal: BufferedImage?)

    /**
     * Called with the results when the pipeline has finished. The supplied images have the same size.
     */
    fun success(matchedNormal: BufferedImage?, matchedAbnormal: BufferedImage?)

}
