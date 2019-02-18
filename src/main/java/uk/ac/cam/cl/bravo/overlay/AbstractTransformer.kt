package uk.ac.cam.cl.bravo.overlay

import java.awt.Point
import java.awt.image.BufferedImage

abstract class AbstractTransformer(
    /** Higher scale means lower absolute values, hence higher weight when minimising */
    override val parameterScale: Double,
    /** Higher value means higher penalisation for adjusting parameters of this transformer */
    override val parameterPenaltyScale: Double
) : Transformer {

    protected fun rescaleOut(parameters: List<Double>) = parameters.map { it / parameterScale }
    protected fun rescaleIn(parameters: DoubleArray) = parameters.map { it * parameterScale }.toDoubleArray()

    protected abstract val initialGuess0: List<Double>
    override val initialGuess: List<Double> get() = rescaleOut(initialGuess0)

    protected abstract val minBounds0: List<Double>
    override val minBounds: List<Double> get() = rescaleOut(minBounds0)

    protected abstract val maxBounds0: List<Double>
    override val maxBounds: List<Double> get() = rescaleOut(maxBounds0)

    abstract fun transform0(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage
    override fun transform(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage =
        transform0(image, rescaleIn(parameters), planeSize)

}
