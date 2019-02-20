package uk.ac.cam.cl.bravo.overlay

import java.awt.Point
import java.awt.image.BufferedImage

/**
 * Calculates a relative value of how good a match between two images (base, sample) is.
 * A function that the ImageOverlay tries to minimise.
 */
@FunctionalInterface
interface OverlayFunction {
    fun value(
        base: BufferedImage,
        sample: BufferedImage,
        planeSize: Point,
        penaltyScaledParameters: Iterable<Double>
    ): Double


    operator fun plus(other: OverlayFunction): OverlayFunction = object : OverlayFunction {
        override fun value(
            base: BufferedImage,
            sample: BufferedImage,
            planeSize: Point,
            penaltyScaledParameters: Iterable<Double>
        ) =
            this@OverlayFunction.value(base, sample, planeSize, penaltyScaledParameters) +
                    other.value(base, sample, planeSize, penaltyScaledParameters)
    }

    operator fun times(other: Double): OverlayFunction = object : OverlayFunction {
        override fun value(
            base: BufferedImage,
            sample: BufferedImage,
            planeSize: Point,
            penaltyScaledParameters: Iterable<Double>
        ) =
            this@OverlayFunction.value(base, sample, planeSize, penaltyScaledParameters) * other
    }
}
