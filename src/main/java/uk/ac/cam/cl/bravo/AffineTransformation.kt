package uk.ac.cam.cl.bravo

/**
 * Affine transformation is written as follows:
 *
 *   | x2 |     | a   b |   | x1 |     | e |
 *   |    |  =  |       | x |    |  +  |   |
 *   | y2 |     | c   d |   | x2 |     | f |
 *
 * Where (a, b, c, d) define rotation and scaling and (e, f) define translation.
 *
 * Parameters are [a, b, c, d, e, f]
 */
class AffineTransformation(parameters: IntArray)
