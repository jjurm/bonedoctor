package uk.ac.cam.cl.bravo.dataset

/** A classified sample loaded from the MURA dataset. */
class ImageSample(val path: String, val patient: Int, val bodypart: Bodypart, val normality: Normality)
