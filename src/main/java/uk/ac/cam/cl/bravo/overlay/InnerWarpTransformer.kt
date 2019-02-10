package uk.ac.cam.cl.bravo.overlay

import com.jhlabs.image.WarpFilter
import com.jhlabs.image.WarpGrid
import uk.ac.cam.cl.bravo.util.ImageTools
import java.awt.Color
import java.awt.image.BufferedImage

class InnerWarpTransformer(
    parameterScale: Double,
    parameterPenaltyScale: Double,
    /** number of flexible points in one dimension */
    private val RESOLUTION: Int
) : AbstractTransformer(parameterScale, parameterPenaltyScale) {

    // number of flexible+fixed points in one dimension (points at the edges are fixed)
    private val SIZE = RESOLUTION + 2
    // flexible points (in principle RESOLUTION in two dimensions)
    private val FLEXIBLE = RESOLUTION * RESOLUTION
    // total number of points in the grid
    private val GRID_POINTS = SIZE * SIZE
    private val SOURCE_GRID = WarpGrid(SIZE, SIZE, ImageTools.PLANE_WIDTH, ImageTools.PLANE_HEIGHT)

    override val parameterCount get() = FLEXIBLE * 2 // X, Y coordinates for each grid point

    override val initialGuess0
        get() = List(FLEXIBLE * 2) { 0.0 }
    override val minBounds0 get() = List(FLEXIBLE * 2) { -1.0 }
    override val maxBounds0 get() = List(FLEXIBLE * 2) { 1.0 }

    private fun paramsToGrid(parameters: DoubleArray): WarpGrid {
        val dstGrid = WarpGrid(SIZE, SIZE, ImageTools.PLANE_WIDTH, ImageTools.PLANE_HEIGHT)
        (0 until FLEXIBLE).forEach { i ->
            val x = i % RESOLUTION
            val y = i / RESOLUTION
            dstGrid.xGrid[(y + 1) * SIZE + (x + 1)] += (parameters[i] * ImageTools.PLANE_WIDTH).toFloat()
            dstGrid.yGrid[(y + 1) * SIZE + (x + 1)] += (parameters[FLEXIBLE + i] * ImageTools.PLANE_HEIGHT).toFloat()
        }
        return dstGrid
    }

    override fun transform0(image: BufferedImage, parameters: DoubleArray): BufferedImage {
        val filter = WarpFilter(SOURCE_GRID, paramsToGrid(parameters))

        return filter.filter(
            image,
            BufferedImage(ImageTools.PLANE_WIDTH, ImageTools.PLANE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY)
        )
    }

    fun drawMarks(image: BufferedImage, params: DoubleArray) {
        val grid = paramsToGrid(rescaleIn(params))
        val size = 5
        fun pointX(x: Int, y: Int) = grid.xGrid[y * SIZE + x].toInt()
        fun pointY(x: Int, y: Int) = grid.yGrid[y * SIZE + x].toInt()

        ImageTools.withGraphics(image) {
            color = Color.blue
            for (y in 0 until SIZE) {
                for (x in 0 until SIZE) {
                    fillOval(
                        pointX(x, y) - size / 2,
                        pointY(x, y) - size / 2,
                        size,
                        size
                    )
                }
            }
            // vertical lines
            for (y in 0 until (SIZE - 1)) {
                for (x in 0 until SIZE) {
                    drawLine(pointX(x, y), pointY(x, y), pointX(x, y + 1), pointY(x, y + 1))
                }
            }
            // horizontal lines
            for (y in 0 until SIZE) {
                for (x in 0 until (SIZE - 1)) {
                    drawLine(pointX(x, y), pointY(x, y), pointX(x + 1, y), pointY(x + 1, y))
                }
            }
        }
    }
}
