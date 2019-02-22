package uk.ac.cam.cl.bravo.pipeline

class Uncertain<T>(val value: T, val confidence: Confidence) {
    @Deprecated("Don't use the default value constructor. Use Uncertain<T>(T, Confidence) instead.")
    constructor(value: T) : this(value, Confidence.HIGH)
}
