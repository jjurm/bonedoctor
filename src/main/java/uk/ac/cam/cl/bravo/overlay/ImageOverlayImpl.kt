package uk.ac.cam.cl.bravo.overlay

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.PointValuePair
import org.apache.commons.math3.optim.SimpleBounds
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer
import uk.ac.cam.cl.bravo.util.ImageTools
import java.awt.image.BufferedImage

class ImageOverlayImpl(
    private val transformers: Array<Transformer>,
    private val f: SimilarityFunction
) : ImageOverlay {

    private val parameterCount = transformers.map { it.parameterCount }.sum()
    private val initialGuess = InitialGuess(transformers.map { it.initialGuess }.flatten().toDoubleArray())
    private val bounds = SimpleBounds(
        transformers.map { it.minBounds }.flatten().toDoubleArray(),
        transformers.map { it.maxBounds }.flatten().toDoubleArray()
    )

    fun Array<Transformer>.transformAll(image: BufferedImage, parameters: DoubleArray): BufferedImage {
        // for each transformer, extract the appropriate parameters and transform the image
        val (_, transformed) = transformers.fold(Pair(0, image)) { (paramOffset, image), transformer ->
            val sliced = parameters.slice(paramOffset until (transformer.parameterCount + paramOffset))
            val transformed = transformer.transform(image, sliced.toDoubleArray())
            Pair(paramOffset + transformer.parameterCount, transformed)
        }
        return transformed
    }

    fun find(
        base: BufferedImage,
        sample: BufferedImage
    ): PointValuePair {
        val optimizer = BOBYQAOptimizer(parameterCount * 2 + 1, 15.0, 1e-9)
        val baseInPlane = ImageTools.copyToPlane(base)

        val result = optimizer.optimize(
            ObjectiveFunction { params ->
                f.value(baseInPlane, transformers.transformAll(sample, params))
            },
            GoalType.MINIMIZE,
            initialGuess,
            bounds,
            MaxEval(1000)
        )

        println("Evaluations: ${optimizer.evaluations}")
        return result
    }

    override fun findBestOverlay(base: BufferedImage, sample: BufferedImage): BufferedImage {
        val result = find(base, sample)
        val parameters = result.point.toList().map { "%.3f".format(it) }
        println("Transform: ${parameters}")
        println("Similarity: ${result.value}")
        return transformers.transformAll(sample, result.point)
    }

}
