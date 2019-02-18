package uk.ac.cam.cl.bravo.overlay

import java.awt.Point
import java.awt.image.BufferedImage

interface Transformer {

    val parameterCount: Int
    val parameterScale: Double
    val parameterPenaltyScale: Double

    val initialGuess: List<Double>
    val minBounds: List<Double>
    val maxBounds: List<Double>

    fun transform(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage

}
