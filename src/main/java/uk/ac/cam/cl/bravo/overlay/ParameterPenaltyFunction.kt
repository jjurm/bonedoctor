package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.dataset.BodypartView
import java.awt.Point
import java.awt.image.BufferedImage

class ParameterPenaltyFunction(
    private val power: Double
) : OverlayFunction {

    override fun value(
        base: BufferedImage,
        bodypartView: BodypartView,
        sample: BufferedImage,
        planeSize: Point,
        penaltyScaledParameters: Iterable<Double>
    ): Double {
        return penaltyScaledParameters.map { Math.pow(Math.abs(it), power) }.sum()
    }

}
