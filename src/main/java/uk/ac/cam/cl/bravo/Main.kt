package uk.ac.cam.cl.bravo

import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.BoneCondition
import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.gui.DisplayImage
import uk.ac.cam.cl.bravo.pipeline.MainPipeline
import java.awt.Point
import java.lang.IllegalArgumentException

const val PLANE_WIDTH = 520
const val PLANE_HEIGHT = PLANE_WIDTH
val PLANE_SIZE = Point(PLANE_WIDTH, PLANE_HEIGHT)

fun main(args: Array<String>) {
    //val (file1, file2) =
    //    "images/in/train_XR_HAND_patient09734_study1_positive_image1_edit.png" to "images/in/train_XR_HAND_patient09734_study1_positive_image3_edit.png"
    //    "images/in/train_XR_FOREARM_patient02116_study1_negative_image1.png" to "images/in/train_XR_FOREARM_patient02132_study1_negative_image1.png"
    //    "images/in/train_XR_SHOULDER_patient00037_study1_positive_image1_edit.png" to "images/in/train_XR_SHOULDER_patient01449_study1_negative_image2_edit.png"
    //tryOverlay(file1, file2)

    val (filename, bodypart) = when (args.size) {
        0 -> {
            val dataset = Dataset()
            val imageSample = dataset.training.values
                //.filter { it.bodypartView.bodypart == Bodypart.HAND && it.patient == 276 && it.boneCondition == BoneCondition.ABNORMAL }.get(1)
                //.filter { it.bodypartView.bodypart == Bodypart.HAND && it.patient == 1863 && it.boneCondition == BoneCondition.ABNORMAL }.get(2)
                //.filter { it.bodypartView.bodypart == Bodypart.HAND && it.patient == 1870 && it.boneCondition == BoneCondition.ABNORMAL }.get(0)

                // Hand with metal part
                .filter { it.bodypartView.bodypart == Bodypart.HAND && it.patient == 1928 && it.boneCondition == BoneCondition.ABNORMAL }.get(1)
            imageSample.path to imageSample.bodypartView.bodypart
        }
        2 -> {
            args[0] to Bodypart.valueOf(args[1].toUpperCase())
        }
        else -> {
            throw IllegalArgumentException("need 2 arguments: <filename> <bodypart>")
        }
    }

    mainPipeline(filename, bodypart)
}

/*fun preprocessPipeline() {
    val dataset = Dataset()

//    val imagePreprocessor: ImagePreprocessor = ImagePreprocessorI()
    val bodypartViewClassifierImpl: BodypartViewClassifier = BodypartViewClassifierImpl()

    listOf(dataset.training, dataset.validation).map { it.values }.flatten().forEach { sample ->
        //var image = sample.loadImage()

        // preprocessing
//        val image = imagePreprocessor.preprocess(sample.path)

        // classify view
//        val view = bodypartViewClassifierImpl.classify(image, sample.bodypart)

        val newPath = sample.path.removeSuffix(".png") + "_edit.png"
//        ImageIO.write(image, "png", File(newPath))
    }
}*/

fun mainPipeline(inputFile: String, bodypart: Bodypart) {
    DisplayImage(inputFile, "Input")

    val pipeline = MainPipeline()

    pipeline.status.subscribe({status -> println("Status: $status")})
    pipeline.preprocessed.subscribe { DisplayImage(it.value, "Preprocessed (confidence: ${it.confidence})") }
    pipeline.boneCondition.subscribe { println("BoneCondition: ${it.value}, confidence: ${it.confidence}") }

    pipeline.similarNormal.subscribe { it.take(4).forEachIndexed { i, img ->
        println("Similar $i: ${img.value.path}")
        DisplayImage(img.value.preprocessedPath, "Similar $i (score: ${img.score})")
    } }

    //pipeline.transformedAndOverlaidOriginal.subscribe {DisplayImage(it.value.second, "OverlaidOriginal (score: ${it.score}")}
    //pipeline.transformedAndOverlaidMirrored.subscribe {DisplayImage(it.value.second, "OverlaidMirrored (score: ${it.score}")}
    pipeline.firstOverlaid.subscribe {
        //DisplayImage(it.value.first, "Transformed (score: ${it.score})")
        DisplayImage(it.value.second, "Overlaid (score: ${it.score})")
    }
    //pipeline.overlaid.subscribe { DisplayImage(it.value, "Overlaid (best) (score: ${it.score})") }
    pipeline.fracturesHighlighted.subscribe { DisplayImage(it, "Fractures highlighted") }

    pipeline.overlaidDifferences.subscribe { DisplayImage(it, "Overlaid differences") }

    pipeline.userInput.onNext(Pair(inputFile, bodypart))
}

/*fun loadDataset() {
    val dataset = Dataset()
    val subset = dataset.training.filter { it.normality == Normality.positive && it.bodypart == Bodypart.HAND }
}*/

/*fun tryOverlay(file1: String, file2: String) {
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
        bigPlaneSize = PLANE_SIZE
    )

    println("Fitting images...")
    val sw = StopWatch()
    sw.start()
    val result = overlay.findBestOverlay(base, sample, downsample, 1e-5)
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
}*/
