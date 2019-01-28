package uk.ac.cam.cl.bravo.dataset

enum class Normality(val value: Boolean) {
    /** Negative means Normal */
    negative(false),

    /** Positive means Abnormal */
    positive(true);

}
