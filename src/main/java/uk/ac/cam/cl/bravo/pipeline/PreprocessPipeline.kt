package uk.ac.cam.cl.bravo.pipeline

import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.dataset.ImageSample
import uk.ac.cam.cl.bravo.hash.ImageMatcherImpl
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    PreprocessPipeline().run(args)
}

class PreprocessPipeline {

    private lateinit var dataset: Dataset

    fun run(args: Array<String>) {
        val tasks: Map<String, () -> Unit> = mapOf(
            "images" to ::preprocessImages,
            "matcher" to ::trainImageMatcher
        )
        val toRun = ArrayList<Pair<String, () -> Unit>>()
        if ("all" in args) {
            toRun.addAll(tasks.toList())
        } else {
            // check if all arguments are known
            args.asList().forEach { arg ->
                if (arg !in tasks.keys)
                    throw IllegalArgumentException("Task '$arg' not known")
            }
            // add all tasks that are present in the list of arguments
            toRun.addAll(tasks.filter { (name, _) -> name in args }.toList())
        }

        if (toRun.isNotEmpty()) {
            dataset = Dataset()
            for ((name, task) in toRun) {
                println("Running task: $name")
                task()
            }
            println("Done")
        } else {
            println(
                """There are no tasks to be run.
                    |Usage:
                    |   gradlew preprocess --args '<tasks>'
                    |Possible tasks are: all, ${tasks.keys.joinToString()}
                """.trimMargin()
            )
        }
    }

    private fun preprocessImages() {
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val imagePreprocessor: ImagePreprocessor = ImagePreprocessorI {}

        class ImagePreprocessorTask(private val path: String, private val imageSample: ImageSample) : Runnable {
            override fun run() {
                try {
                    val file = File(imageSample.preprocessedPath)
                    if (file.exists()) return

                    println("Preprocessing $path")
                    val image = imagePreprocessor.preprocess(path)
                    file.parentFile.mkdirs()
                    ImageIO.write(image.value, "png", file)
                } catch (e: Exception) {
                    System.err.println("Preprocessing $path failed:")
                    e.printStackTrace()
                }
            }
        }

        dataset.combined.forEach { (path, imageSample) ->
            executor.submit(ImagePreprocessorTask(path, imageSample))
        }

        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.DAYS)
    }

    private fun trainImageMatcher() {
        File(Dataset.DIR_PREPROCESSED).mkdirs()
        ImageMatcherImpl.trainImageMatcher(File(Dataset.IMAGE_MATCHER_FILE), dataset)
    }


}
