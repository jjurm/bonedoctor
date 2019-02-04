package uk.ac.cam.cl.bravo.overlay

import java.awt.image.BufferedImage

abstract class AbstractTransformer : Transformer {

    /** Higher scale means lower absolute values, hence higher weight when minimising */
    protected abstract val parameterScale: Double

    protected fun rescaleOut(parameters: List<Double>) = parameters.map { it / parameterScale }
    protected fun rescaleIn(parameters: DoubleArray) = parameters.map { it * parameterScale }.toDoubleArray()

    protected abstract val initialGuess0: List<Double>
    override val initialGuess: List<Double> get() = rescaleOut(initialGuess0)

    protected abstract val minBounds0: List<Double>
    override val minBounds: List<Double> get() = rescaleOut(minBounds0)

    protected abstract val maxBounds0: List<Double>
    override val maxBounds: List<Double> get() = rescaleOut(maxBounds0)

    abstract fun transform0(image: BufferedImage, parameters: DoubleArray): BufferedImage
    override fun transform(image: BufferedImage, parameters: DoubleArray): BufferedImage =
        transform0(image, rescaleIn(parameters))

}
