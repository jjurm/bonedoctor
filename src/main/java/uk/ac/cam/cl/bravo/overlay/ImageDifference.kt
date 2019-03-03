package uk.ac.cam.cl.bravo.overlay

import java.awt.Color
import java.awt.image.BufferedImage
import java.util.function.BiFunction

/**
 * Highlights differences in pixels between the two given images
 */
class ImageDifference : BiFunction<BufferedImage, BufferedImage, BufferedImage> {

    /**
     * Highlights differences in pixels between the two given images.
     * The images must have the same dimensions.
     */
    override fun apply(a: BufferedImage, b: BufferedImage): BufferedImage {
        val target = BufferedImage(a.width, a.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until a.height) {
            for (x in 0 until a.width) {
                val pixelA = Color(a.getRGB(x, y)).red
                val pixelB = Color(b.getRGB(x, y)).red

                val d = pixelA - pixelB
                val color = if (pixelA > pixelB)
                    Color(d, d * 200 / 255, 0)
                else Color(-d, (-d) * 175 / 255, (-d) * 175 / 255)

                target.setRGB(x, y, color.rgb)
            }
        }
        return target
    }

}
