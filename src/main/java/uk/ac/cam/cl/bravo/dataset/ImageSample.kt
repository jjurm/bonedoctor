package uk.ac.cam.cl.bravo.dataset

import java.io.File
import javax.imageio.ImageIO

/** A classified sample loaded from the MURA dataset. */
class ImageSample(val path: String, val patient: Int, val bodypart: Bodypart, val normality: Normality) {
    val file get() = File(path)
    fun loadImage() = ImageIO.read(file)
}
