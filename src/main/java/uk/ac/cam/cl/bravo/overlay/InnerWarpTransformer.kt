package uk.ac.cam.cl.bravo.overlay

import com.jhlabs.image.WarpFilter
import com.jhlabs.image.WarpGrid
import uk.ac.cam.cl.bravo.util.ImageTools
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

class InnerWarpTransformer(
    parameterScale: Double,
    parameterPenaltyScale: Double,
    /** number of flexible points in one dimension */
    private val resolution: Int
) : AbstractTransformer(parameterScale, parameterPenaltyScale) {

    // number of flexible+fixed points in one dimension (points at the edges are fixed)
    private val SIZE = resolution + 2
    // flexible points (in principle resolution in two dimensions)
    private val FLEXIBLE = resolution * resolution
    // total number of points in the grid
    private val GRID_POINTS = SIZE * SIZE

    override val parameterCount get() = FLEXIBLE * 2 // X, Y coordinates for each grid point

    override val initialGuess0
        get() = List(FLEXIBLE * 2) { 0.0 }
    override val minBounds0 get() = List(FLEXIBLE * 2) { -1.0 }
    override val maxBounds0 get() = List(FLEXIBLE * 2) { 1.0 }

    private fun paramsToGrid(parameters: DoubleArray, planeSize: Point): WarpGrid {
        val dstGrid = WarpGrid(SIZE, SIZE, planeSize.x, planeSize.y)
        (0 until FLEXIBLE).forEach { i ->
            val x = i % resolution
            val y = i / resolution
            dstGrid.xGrid[(y + 1) * SIZE + (x + 1)] += (parameters[i] * planeSize.x).toFloat()
            dstGrid.yGrid[(y + 1) * SIZE + (x + 1)] += (parameters[FLEXIBLE + i] * planeSize.y).toFloat()
        }
        return dstGrid
    }

    override fun transform0(image: BufferedImage, parameters: DoubleArray, planeSize: Point): BufferedImage {
        val srcGrid = WarpGrid(SIZE, SIZE, planeSize.x, planeSize.y)
        val dstGrid = paramsToGrid(parameters, planeSize)
        val filter = WarpFilter(srcGrid, dstGrid)

        return filter.filter(image, ImageTools.getPlaneImage(planeSize))
    }

    fun drawMarks(image: BufferedImage, params: DoubleArray, planeSize: Point) {
        val grid = paramsToGrid(rescaleIn(params), planeSize)
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
