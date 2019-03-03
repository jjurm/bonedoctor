package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.BodypartView
import uk.ac.cam.cl.bravo.util.Tuple4
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
        bodypartView: BodypartView,
        sample: BufferedImage,
        planeSize: Point,
        penaltyScaledParameters: Iterable<Double>
    ): Double {

        // image is greyscale => can take any channel from rgb
        fun BufferedImage.pixel(x: Int, y: Int) = Color(getRGB(x, y)).red

        val bodypart = bodypartView.bodypart
        val (ignoreBorderTop, ignoreBorderRight, ignoreBorderBottom, ignoreBorderLeft) =
            when (bodypart) {
                Bodypart.HAND -> Tuple4(false, false, true, false)
                else -> Tuple4(true, true, true, true)
            }

        val fromX = (planeSize.x * (if (ignoreBorderLeft) ignoreBorderWidth else 0.0)).roundToInt()
        val toX = (planeSize.x * (1 - (if (ignoreBorderRight) ignoreBorderWidth else 0.0))).roundToInt()
        val fromY = (planeSize.y * (if (ignoreBorderTop) ignoreBorderWidth else 0.0)).roundToInt()
        val toY = (planeSize.y * (1 - (if (ignoreBorderBottom) ignoreBorderWidth else 0.0))).roundToInt()

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
