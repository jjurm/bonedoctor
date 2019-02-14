package uk.ac.cam.cl.bravo.util

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

object ImageTools {

    fun getPlaneImage(width: Int, height: Int) = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    fun getPlaneImage(size: Point) = BufferedImage(size.x, size.y, BufferedImage.TYPE_BYTE_GRAY)

    fun withGraphics(image: BufferedImage, function: Graphics2D.() -> Unit) {
        val g2d = image.createGraphics()
        try {
            function(g2d)
        } finally {
            g2d.dispose()
        }
    }

    fun copyToPlane(
        image: BufferedImage,
        planeSize: Point,
        plane: BufferedImage = getPlaneImage(planeSize),
        downsampleImage: Double = 1.0
    ): BufferedImage {
        val size = Point(image.width, image.height) / downsampleImage
        withGraphics(plane) {
            drawImage(
                image,
                planeSize.x / 2 - size.x / 2,
                planeSize.y / 2 - size.y / 2,
                size.x,
                size.y,
                Color.BLACK,
                null
            )
        }
        return plane
    }

    /**
     * Overlay two images, given the transform of the sample
     */
    fun overlay(base: BufferedImage, transformedSample: BufferedImage, planeSize: Point): BufferedImage {
        val plane1 = copyToPlane(base, planeSize)
        val plane2 = copyToPlane(transformedSample, planeSize)

        val plane = BufferedImage(planeSize.x, planeSize.y, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until planeSize.y) {
            for (x in 0 until planeSize.x) {
                plane.setRGB(
                    x, y, Color(
                        Color(plane1.getRGB(x, y)).red,
                        Color(plane2.getRGB(x, y)).red,
                        0
                    ).rgb
                )
            }
        }
        return plane
    }

}

operator fun Point.times(scale: Double) = Point((x * scale).toInt(), (y * scale).toInt())
operator fun Point.div(scale: Int) = Point(x / scale, y / scale)
operator fun Point.div(scale: Double) = Point((x / scale).roundToInt(), (y / scale).roundToInt())
fun Point.area() = x * y
