package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.util.ImageTools.getPlaneImage
import uk.ac.cam.cl.bravo.util.ImageTools.withGraphics
import java.awt.Point
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

class AffineTransformer(
    parameterScale: Double,
    parameterPenaltyScale: Double
) : AbstractTransformer(parameterScale, parameterPenaltyScale) {

    companion object {
        private const val REL_BOUND = 1.0
        private const val ABS_BOUND = 1.0
    }

    override val parameterCount get() = 6

    val identity = listOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    override val initialGuess0 get() = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    override val minBounds0
        get() = listOf(
            -REL_BOUND,
            -REL_BOUND,
            -REL_BOUND,
            -REL_BOUND,
            -ABS_BOUND,
            -ABS_BOUND
        )
    override val maxBounds0 get() = listOf(REL_BOUND, REL_BOUND, REL_BOUND, REL_BOUND, ABS_BOUND, ABS_BOUND)

    /**
     * When applying an affine transform, the centre of the transform is the top left point of the image. To apply
     * the transformation with (0,0) in the centre of the image, we must translate the transform first
     */
    private fun calculateInPlaneTransform(
        image: BufferedImage,
        affineTransform: AffineTransform,
        planeSize: Point
    ): AffineTransform {
        val transform = AffineTransform()
        transform.translate((planeSize.x / 2).toDouble(), (planeSize.y / 2).toDouble())
        transform.concatenate(affineTransform)
        transform.translate((-planeSize.x / 2).toDouble(), (-planeSize.y / 2).toDouble())
        transform.translate(
            (planeSize.x / 2 - image.width / 2).toDouble(),
            (planeSize.y / 2 - image.height / 2).toDouble()
        )
        return transform
    }

    override fun transform0(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage {
        val plane = getPlaneImage(planeSize)
        val matrix = (parameters zip identity).map { (a, b) -> a + b }.toDoubleArray()
        matrix[4] *= planeSize.x.toDouble() / 2
        matrix[5] *= planeSize.y.toDouble() / 2
        val transform = calculateInPlaneTransform(image, AffineTransform(matrix), planeSize)
        withGraphics(plane) {
            drawImage(image, transform, null)
        }
        return plane
    }

    override fun scaleParametersToPenalty(parameters: Iterable<Double>): DoubleArray {
        var cp = super.scaleParametersToPenalty(parameters)

        // don't penalise translation
        cp[4] = 0.0
        cp[5] = 0.0

        // don't penalise rotation
        val matrix = (cp zip identity).map { (a, b) -> a + b }.toDoubleArray()
        val middlePointX = (matrix[0] + matrix[2]) / 2
        val middlePointY = (matrix[1] + matrix[3]) / 2
        val rotation = Math.atan2(middlePointY, middlePointX) - Math.PI / 4
        val transform = AffineTransform(matrix)
        transform.rotate(-rotation)
        transform.getMatrix(cp)
        cp = (cp zip identity).map { (a,b) -> a - b }.toDoubleArray()

        return cp
    }

}
