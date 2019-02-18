package uk.ac.cam.cl.bravo.overlay

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.PointValuePair
import org.apache.commons.math3.optim.SimpleBounds
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer
import uk.ac.cam.cl.bravo.util.ImageTools
import uk.ac.cam.cl.bravo.util.div
import java.awt.Point
import java.awt.image.BufferedImage

class ImageOverlayImpl(
    private val transformers: Array<Transformer>,
    private val f: OverlayFunction,
    private val bigPlaneSize: Point,
    private val downsample: Double = 1.0,
    private val precision: Double = 1e-4
) : ImageOverlay {

    private val smallPlaneSize = bigPlaneSize / downsample
    private val parameterCount = transformers.map { it.parameterCount }.sum()
    private val initialGuess = InitialGuess(transformers.map { it.initialGuess }.flatten().toDoubleArray())
    private val bounds = SimpleBounds(
        transformers.map { it.minBounds }.flatten().toDoubleArray(),
        transformers.map { it.maxBounds }.flatten().toDoubleArray()
    )

    fun Array<Transformer>.transformAll(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage {
        // for each transformer, extract the appropriate parameters and transform the image
        val (_, transformed) = this.fold(Pair(0, image)) { (paramOffset, image), transformer ->
            val sliced = parameters.slice(paramOffset until (transformer.parameterCount + paramOffset))
            val transformed = transformer.transform(image, sliced.toDoubleArray(), planeSize)
            Pair(paramOffset + transformer.parameterCount, transformed)
        }
        return transformed
    }

    fun penaltyScaledParameters(parameters: DoubleArray): Iterable<Double> {
        val accParams = ArrayList<Double>(parameterCount)
        transformers.fold(0) { paramOffset, transformer ->
            val s = transformer.parameterScale * transformer.parameterPenaltyScale
            for (i in paramOffset until (paramOffset + transformer.parameterCount)) {
                accParams.add(parameters[i] * s)
            }
            paramOffset + transformer.parameterCount
        }
        return accParams
    }

    override fun findBestOverlay(base: BufferedImage, sample: BufferedImage): PointValuePair {
        val optimizer = BOBYQAOptimizer(
            parameterCount * 2 + 1,
            0.3,
            precision
        )
        val baseInPlane = ImageTools.copyToPlane(base, smallPlaneSize, downsampleImage = downsample)
        val sampleInPlane = ImageTools.copyToPlane(sample, smallPlaneSize, downsampleImage = downsample)
        val result = optimizer.optimize(
            ObjectiveFunction { params ->
                val transformed = transformers.transformAll(sampleInPlane, params, smallPlaneSize)
                f.value(baseInPlane, transformed, smallPlaneSize, penaltyScaledParameters(params))
            },
            GoalType.MINIMIZE,
            initialGuess,
            bounds,
            MaxEval(2000)
        )

        println("Evaluations: ${optimizer.evaluations}")
        println("Parameters: ${result.point.toList().map { "%.3f".format(it) }}")
        println("Similarity: ${result.value}")
        return result
    }

    /**
     * Applies the given transformation without downscaling the image or the underlying plane.
     */
    fun applyTransformations(image: BufferedImage, parameters: DoubleArray): BufferedImage {
        val inPlane = ImageTools.copyToPlane(image, bigPlaneSize)
        return transformers.transformAll(inPlane, parameters, bigPlaneSize)
    }

}
