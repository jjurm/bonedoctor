package uk.ac.cam.cl.bravo.pipeline

/**
 * Use this class to state a confidence value of a result.
 * If there is no information about the confidence, use HIGH.
 */
enum class Confidence {
    HIGH, MEDIUM, LOW;

    override fun toString(): String {
        return super.toString().toLowerCase().capitalize()
    }
}
