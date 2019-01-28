package uk.ac.cam.cl.bravo

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.PointValuePair
import org.apache.commons.math3.optim.SimpleBounds
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.lang.Math.max
import java.math.BigDecimal

class ImageOverlayImpl : ImageOverlay {

    companion object {
        private const val PLANE_WIDTH = 550
        private const val PLANE_HEIGHT = PLANE_WIDTH
        private const val REL_BOUND = 3.0
        private const val ABS_BOUND = (PLANE_WIDTH / 2).toDouble()

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

        /**
         * Copy the image to a bigger plane of a fixed size, and optionally apply the transform
         */
        private fun copyToPlane(image: BufferedImage, affineTransform: AffineTransform? = null): BufferedImage {
            val plane = BufferedImage(
                PLANE_WIDTH,
                PLANE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY)
            val g2d = plane.createGraphics()
            if (affineTransform == null) {
                g2d.drawImage(
                    image,
                    PLANE_WIDTH / 2 - image.width / 2,
                    PLANE_HEIGHT / 2 - image.height / 2,
                    image.width,
                    image.height,
                    Color.BLACK,
                    null
                )
            } else {
                g2d.drawImage(image,
                    calculateInPlaneTransform(
                        image,
                        affineTransform
                    ), null)
            }
            g2d.dispose()
            return plane
        }

        /**
         * Overlay two images, given the transform of the sample
         */
        fun overlay(base: BufferedImage, sample: BufferedImage, transform: AffineTransform): BufferedImage {
            val plane1 = copyToPlane(base)
            val plane2 = copyToPlane(sample, transform)

            val plane = BufferedImage(
                PLANE_WIDTH,
                PLANE_HEIGHT, BufferedImage.TYPE_INT_RGB)
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

        /**
         * Calculate the similarity of two images, given the transform of the sample
         */
        fun similarity(base: BufferedImage, sample: BufferedImage, transform: AffineTransform): BigDecimal {
            val basePlane = copyToPlane(base)
            val samplePlane = copyToPlane(sample, transform)

            // image is greyscale => can take any channel from rgb
            fun BufferedImage.pixel(x: Int, y: Int) = Color(getRGB(x, y)).red

            var sum = BigDecimal.ZERO
            for (y in 0 until PLANE_HEIGHT) {
                for (x in 0 until PLANE_WIDTH) {
                    val basePixel = basePlane.pixel(x, y)
                    val samplePixel = samplePlane.pixel(x, y)

                    val over = max(samplePixel - basePixel, 0).toDouble()
                    val under = max(basePixel - samplePixel, 0).toDouble()
                    val value = Math.pow(over + under, 2.0) / 100.0
                    sum += BigDecimal.valueOf(value.toLong())
                }
            }
            return sum.divide(BigDecimal.valueOf(1e6)) // rescale to get human-readable values
        }

        /** Helper function */
        fun DoubleArray.toAffineTransform() = AffineTransform(this[0], this[1], this[2], this[3], this[4], this[5])
    }

    class SimilarityFunction(private val base: BufferedImage, private val sample: BufferedImage) :
        MultivariateFunction {
        override fun value(point: DoubleArray): Double {
            return similarity(
                base,
                sample,
                point.toAffineTransform()
            ).toDouble()
        }
    }

    fun find(
        base: BufferedImage,
        sample: BufferedImage,
        initialGuess: DoubleArray = doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    ): PointValuePair {
        val optimizer = BOBYQAOptimizer(20, 15.0, 1e-9)

        val result = optimizer.optimize(
            ObjectiveFunction(SimilarityFunction(base, sample)),
            GoalType.MINIMIZE,
            InitialGuess(initialGuess),
            SimpleBounds(
                doubleArrayOf(-REL_BOUND, -REL_BOUND, -REL_BOUND, -REL_BOUND, -ABS_BOUND, -ABS_BOUND),
                doubleArrayOf(
                    REL_BOUND,
                    REL_BOUND,
                    REL_BOUND,
                    REL_BOUND,
                    ABS_BOUND,
                    ABS_BOUND
                )
            ),
            MaxEval(1000)
        )
        println("Evaluations: ${optimizer.evaluations}")
        return result
    }

    override fun findBestOverlay(base: BufferedImage, sample: BufferedImage): AffineTransform {
        val result = find(base, sample)
        val parameters = result.point.toList().map { "%.3f".format(it) }
        println("Transform: ${parameters}")
        println("Similarity: ${result.value}")
        return result.point.toAffineTransform()
    }

}
