package uk.ac.cam.cl.bravo.dataset

import java.io.File
import javax.imageio.ImageIO

/** A classified sample loaded from the MURA dataset. */
class ImageSample(val path: String, val bodypartView: BodypartView) {

    val patient = getPatient(path)
    val boneCondition = getBoneCondition(path)

    val preprocessedPath: String get() = Dataset.DIR_PREPROCESSED + path.removePrefix(Dataset.DIR)

    fun loadImage() = ImageIO.read(File(path))
    fun loadPreprocessedImage() = ImageIO.read(File(path))

    init {
        if (bodypartView.bodypart !== getBodypart(path))
            throw RuntimeException("BodypartView.bodypart of $path is incorrectly classified as ${bodypartView.bodypart}")
    }

    companion object {
        private val patientRegex = Regex("""patient(\d+)""")
        private val bodypartRegex = Regex("""XR_(\w+)""")
        private val normalityRegex = Regex("""study\d+_(\w+)""")

        private fun getPatient(path: String): Int =
            patientRegex.find(path)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: throw IllegalArgumentException("Cannot determine patient number of $path")

        private fun getBodypart(path: String): Bodypart =
            bodypartRegex.find(path)?.groupValues?.getOrNull(1)?.let { Bodypart.valueOf(it) }
                ?: throw IllegalArgumentException("Cannot determine body part of $path")

        private fun getBoneCondition(path: String): BoneCondition =
            normalityRegex.find(path)?.groupValues?.getOrNull(1)?.let { BoneCondition.fromLabel(it) }
                ?: throw IllegalArgumentException("Cannot determine normality of $path")
    }

}
