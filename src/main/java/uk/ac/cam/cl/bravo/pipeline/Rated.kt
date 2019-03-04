package uk.ac.cam.cl.bravo.pipeline

/**
 * Represents a value with a score (e.g. how good a match is).
 * The score is a relative score of the wrapped sample of the data.
 * Lower score means better.
 */
class Rated<T>(val value: T, val score: Double) : Comparable<Rated<T>> {

    override fun compareTo(other: Rated<T>) = score.compareTo(other.score)

    fun <R> map(mapper: (T) -> R) = Rated(mapper(value), score)

}
