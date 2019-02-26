package uk.ac.cam.cl.bravo.pipeline

import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.hash.ImageMatcherImpl
import java.io.File

fun main() {
    PreprocessPipeline().run()
}

class PreprocessPipeline {

    private val dataset = Dataset()

    fun run() {
        println("Setup pipeline")

        // Train ImageMatcher

        File(Dataset.DIR_PREPROCESSED).mkdirs()
        ImageMatcherImpl.trainImageMatcher(File(Dataset.IMAGE_MATCHER_FILE), dataset)

        println("Done")
    }

}
