package uk.ac.cam.cl.bravo.overlay

import com.jhlabs.image.WarpFilter
import com.jhlabs.image.WarpGrid
import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_HEIGHT
import uk.ac.cam.cl.bravo.util.ImageTools.PLANE_WIDTH
import uk.ac.cam.cl.bravo.util.ImageTools.withGraphics
import java.awt.Color
import java.awt.image.BufferedImage

@Deprecated("Use InnerWarpTransformer, this class is not kept up to date.")
class WarpTransformer(
    parameterScale: Double,
    parameterPenaltyScale: Double
) : AbstractTransformer(parameterScale, parameterPenaltyScale) {

    companion object {
        const val RESOLUTION = 4

        private val GRID_POINTS = RESOLUTION * RESOLUTION
        private val SOURCE_GRID = WarpGrid(RESOLUTION, RESOLUTION, PLANE_WIDTH, PLANE_HEIGHT)
    }

    override val parameterCount get() = GRID_POINTS * 2 // X, Y coordinates for each grid point

    override val initialGuess0 get() = (SOURCE_GRID.xGrid + SOURCE_GRID.yGrid).map { it.toDouble() }
    override val minBounds0 get() = List(GRID_POINTS * 2) { -50.0 }
    override val maxBounds0
        get() =
            List(GRID_POINTS) { (PLANE_WIDTH + 50).toDouble() } + List(GRID_POINTS) { (PLANE_HEIGHT + 100).toDouble() }


    override fun transform0(image: BufferedImage, parameters: DoubleArray): BufferedImage {
        val dstGrid = WarpGrid(RESOLUTION, RESOLUTION, PLANE_WIDTH, PLANE_HEIGHT)

        dstGrid.xGrid = parameters.slice(0 until GRID_POINTS).map { it.toFloat() }.toFloatArray()
        dstGrid.yGrid = parameters.slice(GRID_POINTS until 2 * GRID_POINTS).map { it.toFloat() }.toFloatArray()

        val filter = WarpFilter(SOURCE_GRID, dstGrid)
        val dst = filter.filter(image, BufferedImage(PLANE_WIDTH, PLANE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY))

        return dst
    }

    fun drawMarks(image: BufferedImage, params: DoubleArray) {
        val size = 5
        fun pointX(x: Int, y: Int) = params[y * RESOLUTION + x].toInt()
        fun pointY(x: Int, y: Int) = params[GRID_POINTS + y * RESOLUTION + x].toInt()
        withGraphics(image) {
            color = Color.blue
            for (y in 0 until RESOLUTION) {
                for (x in 0 until RESOLUTION) {
                    fillOval(
                        pointX(x, y) - size / 2,
                        pointY(x, y) - size / 2,
                        size,
                        size
                    )
                }
            }
            // vertical lines
            for (y in 0 until (RESOLUTION - 1)) {
                for (x in 0 until RESOLUTION) {
                    drawLine(pointX(x, y), pointY(x, y), pointX(x, y + 1), pointY(x, y + 1))
                }
            }
            // horizontal lines
            for (y in 0 until RESOLUTION) {
                for (x in 0 until (RESOLUTION - 1)) {
                    drawLine(pointX(x, y), pointY(x, y), pointX(x + 1, y), pointY(x + 1, y))
                }
            }
        }
    }

}

