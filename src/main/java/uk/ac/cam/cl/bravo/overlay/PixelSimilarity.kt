package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_HEIGHT
import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_WIDTH
import java.awt.Color
import java.awt.image.BufferedImage
import java.math.BigDecimal
import kotlin.math.roundToInt

class PixelSimilarity(
    private val parameterPenaltyWeight: Double,
    /**
     * Don't compute similarity at the border. Ignore X many of pixels on all sides (relative to the size of the image).
     * Suitable should be values between 0.05 and 0.30
     */
    private val ignoreBorderWidth: Double
) : SimilarityFunction {

    override fun value(base: BufferedImage, sample: BufferedImage, penaltyScaledParameters: Iterable<Double>): Double {

        // image is greyscale => can take any channel from rgb
        fun BufferedImage.pixel(x: Int, y: Int) = Color(getRGB(x, y)).red

        val fromX = (PLANE_WIDTH * ignoreBorderWidth).roundToInt()
        val toX = (PLANE_WIDTH * (1 - ignoreBorderWidth)).roundToInt()
        val fromY = (PLANE_HEIGHT * ignoreBorderWidth).roundToInt()
        val toY = (PLANE_HEIGHT * (1 - ignoreBorderWidth)).roundToInt()

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
        val fromImage = sum.divide(BigDecimal.valueOf(1e6)).toDouble() // rescale to get human-readable values

        val fromParameters = penaltyScaledParameters.map(Math::abs).sum() * parameterPenaltyWeight

        return fromImage + fromParameters
    }
}
