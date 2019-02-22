package uk.ac.cam.cl.bravo

import com.jhlabs.image.GaussianFilter
import org.apache.commons.lang3.time.StopWatch
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifier
import uk.ac.cam.cl.bravo.classify.BodypartViewClassifierImpl
import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.BoneCondition
import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.gui.DisplayImage
import uk.ac.cam.cl.bravo.overlay.*
import uk.ac.cam.cl.bravo.pipeline.MainPipeline
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI
import uk.ac.cam.cl.bravo.util.ImageTools
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

const val PLANE_WIDTH = 520
const val PLANE_HEIGHT = PLANE_WIDTH
val PLANE_SIZE = Point(PLANE_WIDTH, PLANE_HEIGHT)

fun main(args: Array<String>) {
    val (file1, file2) =
        "images/in/train_XR_HAND_patient09734_study1_positive_image1_edit.png" to "images/in/train_XR_HAND_patient09734_study1_positive_image3_edit.png"
    //    "images/in/train_XR_FOREARM_patient02116_study1_negative_image1.png" to "images/in/train_XR_FOREARM_patient02132_study1_negative_image1.png"
    //    "images/in/train_XR_SHOULDER_patient00037_study1_positive_image1_edit.png" to "images/in/train_XR_SHOULDER_patient01449_study1_negative_image2_edit.png"
    //tryOverlay(file1, file2)

    val dataset = Dataset()
    val imageSample = dataset.training.values
        .filter { it.bodypart == Bodypart.HAND && it.patient == 9734 }.first()
    mainPipeline(imageSample.path, imageSample.bodypart)
}

fun preprocessPipeline() {
    val dataset = Dataset()

    val imagePreprocessor: ImagePreprocessor = ImagePreprocessorI()
    val bodypartViewClassifierImpl: BodypartViewClassifier = BodypartViewClassifierImpl()

    listOf(dataset.training, dataset.validation).map { it.values }.flatten().forEach { sample ->
        //var image = sample.loadImage()

        // preprocessing
        val image = imagePreprocessor.preprocess(sample.path)

        // classify view
        val view = bodypartViewClassifierImpl.classify(image, sample.bodypart)

        val newPath = sample.path.removeSuffix(".png") + "_edit.png"
        ImageIO.write(image, "png", File(newPath))
    }
}

fun mainPipeline(inputFile: String, bodypart: Bodypart) {
    DisplayImage(inputFile, "Input")

    val pipeline = MainPipeline()
    pipeline.status.subscribe(::println)
    pipeline.preprocessed.subscribe { DisplayImage(it) }
    pipeline.overlayed.subscribe { DisplayImage(it) }

    pipeline.userInput.onNext(Pair(inputFile, bodypart))
}

/*fun loadDataset() {
    val dataset = Dataset()
    val subset = dataset.training.filter { it.normality == Normality.positive && it.bodypart == Bodypart.HAND }
}*/

fun tryOverlay(file1: String, file2: String) {
    println("Loading images...")

    val blur = GaussianFilter(2.0f)

    val (base, sample) =
        listOf(file1, file2)
            .map { ImageIO.read(File(it)) }
            .map { blur.filter(it, null) }
            .map { DisplayImage(it); it }


    lateinit var warper: InnerWarpTransformer
    val downsample = 1.0
    val overlay = ImageOverlayImpl(
        arrayOf(
            InnerWarpTransformer(
                parameterScale = 0.8,
                parameterPenaltyScale = 1.0,
                resolution = 4
            ).also { warper = it },
            AffineTransformer(
                parameterScale = 1.0,
                parameterPenaltyScale = 0.1
            )
        ),
        PixelSimilarity(
            ignoreBorderWidth = 0.25
        ) + ParameterPenaltyFunction() * 0.05,
        bigPlaneSize = PLANE_SIZE,
        downsample = downsample,
        precision = 1e-5
    )

    println("Fitting images...")
    val sw = StopWatch()
    sw.start()
    val result = overlay.findBestOverlay(base, sample)
    sw.stop()
    val time = "${"%.1f".format(sw.time.toDouble() / 1000)}s"
    println("  $time")

    println("Generating overlay...")
    val parameters = result.point
    val transformed = overlay.applyTransformations(sample, parameters)
    val overlaid = ImageTools.overlay(base, transformed, PLANE_SIZE)
    warper.drawMarks(overlaid, parameters, PLANE_SIZE)
    DisplayImage(overlaid)

    ImageIO.write(overlaid, "png", File("output.png"))
    println("Done")
}
