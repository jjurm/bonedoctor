package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_HEIGHT
import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_WIDTH
import java.awt.Color
import java.awt.image.BufferedImage
import java.math.BigDecimal

class PixelSimilarity1 : SimilarityFunction {
    override fun value(base: BufferedImage, sample: BufferedImage): Double {

        // image is greyscale => can take any channel from rgb
        fun BufferedImage.pixel(x: Int, y: Int) = Color(getRGB(x, y)).red

        var sum = BigDecimal.ZERO
        for (y in 0 until (PLANE_HEIGHT * 0.8).toInt()) {
            for (x in 0 until PLANE_WIDTH) {
                val basePixel = base.pixel(x, y)
                val samplePixel = sample.pixel(x, y)

                val over = Math.max(samplePixel - basePixel, 0).toDouble()
                val under = Math.max(basePixel - samplePixel, 0).toDouble()
                val value = Math.pow(over + under, 2.0) / 100.0 * (1 - y / PLANE_HEIGHT / 4 * 5)
                sum += BigDecimal.valueOf(value.toLong())
            }
        }
        return sum.divide(BigDecimal.valueOf(1e6)).toDouble() // rescale to get human-readable values
    }
}
