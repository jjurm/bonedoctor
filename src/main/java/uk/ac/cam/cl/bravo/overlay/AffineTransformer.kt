package uk.ac.cam.cl.bravo.overlay

import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_HEIGHT
import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_WIDTH
import uk.ac.cam.cl.bravo.util.ImageTools.getPlaneImage
import uk.ac.cam.cl.bravo.util.ImageTools.withGraphics
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
    private fun calculateInPlaneTransform(image: BufferedImage, affineTransform: AffineTransform): AffineTransform {
        val transform = AffineTransform()
        transform.translate((PLANE_WIDTH / 2).toDouble(), (PLANE_HEIGHT / 2).toDouble())
        transform.concatenate(affineTransform)
        transform.translate((-PLANE_WIDTH / 2).toDouble(), (-PLANE_HEIGHT / 2).toDouble())
        transform.translate(
            (PLANE_WIDTH / 2 - image.width / 2).toDouble(),
            (PLANE_HEIGHT / 2 - image.height / 2).toDouble()
        )
        return transform
    }

    override fun transform0(image: BufferedImage, parameters: DoubleArray): BufferedImage {
        val plane = getPlaneImage()
        val matrix = (parameters zip identity).map { (a, b) -> a + b }.toDoubleArray()
        matrix[4] *= PLANE_WIDTH.toDouble() / 2
        matrix[5] *= PLANE_WIDTH.toDouble() / 2
        val transform = calculateInPlaneTransform(image, AffineTransform(matrix))
        withGraphics(plane) {
            drawImage(image, transform, null)
        }
        return plane
    }

}
