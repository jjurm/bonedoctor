package uk.ac.cam.cl.bravo

import uk.ac.cam.cl.bravo.classify.BodypartViewClassifier
import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.dataset.Normality
import uk.ac.cam.cl.bravo.gui.DisplayImage
import uk.ac.cam.cl.bravo.overlay.AffineTransformer
import uk.ac.cam.cl.bravo.overlay.ImageOverlayImpl
import uk.ac.cam.cl.bravo.overlay.PixelSimilarity1
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.util.ImageTools
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    tryOverlay(
        "MURA-v1.1/train/XR_HAND/patient09734/study1_positive/image1_edit.png",
        "MURA-v1.1/train/XR_HAND/patient09734/study1_positive/image3_edit.png"
    )
}

fun preprocessPipeline() {
    val dataset = Dataset()

    val imagePreprocessor : ImagePreprocessor = TODO()
    val bodypartViewClassifier : BodypartViewClassifier = TODO()

    dataset.training.forEach {sample ->
        //var image = sample.loadImage()

        // preprocessing
        var image = imagePreprocessor.preprocess(sample.path)

        // classify view
        val view = bodypartViewClassifier.classify(image, sample.bodypart)

        val newPath = sample.path.removeSuffix(".png") + "_edit.png"
        ImageIO.write(image, "png", File(newPath))
    }
}

fun loadDataset() {
    val dataset = Dataset()
    val subset = dataset.training.filter { it.normality == Normality.positive && it.bodypart == Bodypart.HAND }
}

fun tryOverlay(file1: String, file2: String) {
    println(file1)
    println(file2)

    val base = ImageIO.read(File(file1))
    val sample = ImageIO.read(File(file2))
    DisplayImage(base)
    DisplayImage(sample)

    val overlay = ImageOverlayImpl(
        arrayOf(AffineTransformer()),
        PixelSimilarity1()
    )
    val transformed = overlay.findBestOverlay(base, sample)

    DisplayImage(ImageTools.overlay(base, transformed))
}
