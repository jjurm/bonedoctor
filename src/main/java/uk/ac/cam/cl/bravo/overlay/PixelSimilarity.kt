package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.util.area
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

class PixelSimilarity(
    /**
     * Don't compute similarity at the border. Ignore X many of pixels on all sides (relative to the size of the image).
     * Suitable should be values between 0.05 and 0.30
     */
    private val ignoreBorderWidth: Double
) : OverlayFunction {

    override fun value(
        base: BufferedImage,
        sample: BufferedImage,
        planeSize: Point,
        penaltyScaledParameters: Iterable<Double>
    ): Double {

        // image is greyscale => can take any channel from rgb
        fun BufferedImage.pixel(x: Int, y: Int) = Color(getRGB(x, y)).red

        val fromX = (planeSize.x * ignoreBorderWidth).roundToInt()
        val toX = (planeSize.x * (1 - ignoreBorderWidth)).roundToInt()
        val fromY = (planeSize.y * ignoreBorderWidth).roundToInt()
        val toY = (planeSize.y * (1 - ignoreBorderWidth)).roundToInt()

        var sum = BigDecimal.ZERO
        for (y in fromY until toY) {
            for (x in fromX until toX) {
                val basePixel = base.pixel(x, y)
                val samplePixel = sample.pixel(x, y)

                val over = Math.max(samplePixel - basePixel, 0).toDouble()
                val under = Math.max(basePixel - samplePixel, 0).toDouble()
                val value = Math.pow(over + under, 2.0) / 100.0
                sum += BigDecimal.valueOf(value.toLong())
            }
        }
        val divisor = BigDecimal.valueOf(planeSize.area().toLong() * 3)
        val result = (sum.divide(divisor, 20, RoundingMode.HALF_EVEN)).toDouble()
        return result // rescale to get human-readable values
    }
}
