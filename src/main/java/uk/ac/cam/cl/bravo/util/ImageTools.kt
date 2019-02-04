package uk.ac.cam.cl.bravo.util

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

object ImageTools {

    const val PLANE_WIDTH = 550
    const val PLANE_HEIGHT = PLANE_WIDTH

    fun getPlaneImage() = BufferedImage(
        PLANE_WIDTH,
        PLANE_HEIGHT,
        BufferedImage.TYPE_BYTE_GRAY
    )

    fun withGraphics(image: BufferedImage, function: Graphics2D.() -> Unit) {
        val g2d = image.createGraphics()
        try {
            function(g2d)
        } finally {
            g2d.dispose()
        }
    }

    fun copyToPlane(image: BufferedImage, plane: BufferedImage = getPlaneImage()): BufferedImage {
        withGraphics(plane) {
            drawImage(
                image,
                PLANE_WIDTH / 2 - image.width / 2,
                PLANE_HEIGHT / 2 - image.height / 2,
                image.width,
                image.height,
                Color.BLACK,
                null
            )
        }
        return plane
    }

    /**
     * Overlay two images, given the transform of the sample
     */
    fun overlay(base: BufferedImage, transformedSample: BufferedImage): BufferedImage {
        val plane1 = copyToPlane(base)
        val plane2 = copyToPlane(transformedSample)

        val plane = BufferedImage(PLANE_WIDTH, PLANE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until PLANE_HEIGHT) {
            for (x in 0 until PLANE_WIDTH) {
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


