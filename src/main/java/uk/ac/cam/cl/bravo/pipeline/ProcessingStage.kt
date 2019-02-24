package uk.ac.cam.cl.bravo.pipeline

/**
 * Used when passing results, to convey one of the two stages:
 * - there is still something being processed / loaded, expect more results here in the future
 * - processing is done, don't expect any more results (unless initiated by the user again)
 */
enum class ProcessingStage {
    LOADING, DONE;
}
