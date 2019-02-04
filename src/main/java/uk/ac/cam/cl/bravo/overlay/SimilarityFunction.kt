package uk.ac.cam.cl.bravo.overlay

import java.awt.image.BufferedImage

interface SimilarityFunction {
    fun value(base: BufferedImage, sample: BufferedImage): Double
}
