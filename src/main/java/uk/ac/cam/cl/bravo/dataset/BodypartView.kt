package uk.ac.cam.cl.bravo.dataset

/** Each Bodypart can have various views, identified by an index within the body part. */
data class BodypartView(val bodypart: Bodypart, val value: Int)
