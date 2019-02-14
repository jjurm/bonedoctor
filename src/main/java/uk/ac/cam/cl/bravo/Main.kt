package uk.ac.cam.cl.bravo

import com.jhlabs.image.GaussianFilter
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifier
import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.gui.DisplayImage
import uk.ac.cam.cl.bravo.overlay.AffineTransformer
import uk.ac.cam.cl.bravo.overlay.ImageOverlayImpl
import uk.ac.cam.cl.bravo.overlay.InnerWarpTransformer
import uk.ac.cam.cl.bravo.overlay.PixelSimilarity
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.util.ImageTools
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    val (file1, file2) =
        "images/in/train_XR_FOREARM_patient02116_study1_negative_image1.png" to "images/in/train_XR_FOREARM_patient02132_study1_negative_image1.png"
    //    "images/in/train_XR_HAND_patient09734_study1_positive_image1_edit.png" to "images/in/train_XR_HAND_patient09734_study1_positive_image3_edit.png"
    //    "images/in/train_XR_SHOULDER_patient00037_study1_positive_image1_edit.png" to "images/in/train_XR_SHOULDER_patient01449_study1_negative_image2_edit.png"
    tryOverlay(file1, file2)
}

fun preprocessPipeline() {
    val dataset = Dataset()

    val imagePreprocessor: ImagePreprocessor = TODO()
    val bodypartViewClassifierImpl: BodypartViewClassifier = TODO()

    dataset.training.forEach { sample ->
        //var image = sample.loadImage()

        // preprocessing
        var image = imagePreprocessor.preprocess(sample.path)

        // classify view
        val view = bodypartViewClassifierImpl.classify(image, sample.bodypart)

        val newPath = sample.path.removeSuffix(".png") + "_edit.png"
        ImageIO.write(image, "png", File(newPath))
    }
}

/*fun loadDataset() {
    val dataset = Dataset()
    val subset = dataset.training.filter { it.normality == Normality.positive && it.bodypart == Bodypart.HAND }
}*/

fun tryOverlay(file1: String, file2: String) {
    var base = ImageIO.read(File(file1))
    var sample = ImageIO.read(File(file2))

    val blur = GaussianFilter(2.0f)
    base = blur.filter(base, null)
    sample = blur.filter(sample, null)

    DisplayImage(base)
    DisplayImage(sample)

    lateinit var warper: InnerWarpTransformer

    val overlay = ImageOverlayImpl(
        arrayOf(
            InnerWarpTransformer(
                parameterScale = 0.8,
                parameterPenaltyScale = 2.0,
                RESOLUTION = 4
            ).also { warper = it },
            AffineTransformer(parameterScale = 1.0, parameterPenaltyScale = 0.1)
        ),
        PixelSimilarity(parameterPenaltyWeight = 1.0, ignoreBorderWidth = 0.25),
        precision = 1e-3
    )

    println("Fitting images...")
    val result = overlay.findBestOverlay(base, sample)

    println("Generating overlay...")
    val parameters = result.point
    val transformed = overlay.applyTransformations(sample, parameters)
    val overlaid = ImageTools.overlay(base, transformed)
    warper.drawMarks(overlaid, parameters)
    DisplayImage(overlaid)

    ImageIO.write(overlaid, "png", File("output.png"))
    println("Done")
}
