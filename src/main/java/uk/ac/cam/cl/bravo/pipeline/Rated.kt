package uk.ac.cam.cl.bravo.pipeline

/**
 * Represents a value with a score (e.g. how good a match is).
 * The score is a value between 0.0 and 1.0.
 */
class Rated<T>(val value: T, val score: Double) : Comparable<Rated<T>> {

    override fun compareTo(other: Rated<T>) = score.compareTo(other.score)

}
