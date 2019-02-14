package uk.ac.cam.cl.bravo.overlay

import java.awt.Point
import java.awt.image.BufferedImage

class ParameterPenaltyFunction : OverlayFunction {

    override fun value(base: BufferedImage, sample: BufferedImage, planeSize: Point, penaltyScaledParameters: Iterable<Double>): Double {
        return penaltyScaledParameters.map(Math::abs).sum()
    }

}
