package uk.ac.cam.cl.bravo

import uk.ac.cam.cl.bravo.dataset.Bodypart
import uk.ac.cam.cl.bravo.dataset.Dataset
import uk.ac.cam.cl.bravo.dataset.Normality
import uk.ac.cam.cl.bravo.gui.DisplayImage
import uk.ac.cam.cl.bravo.overlay.ImageOverlayImpl
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    tryOverlay(
        "MURA-v1.1/train/XR_HAND/patient09734/study1_positive/image1_edit.png",
        "MURA-v1.1/train/XR_HAND/patient09734/study1_positive/image3_edit.png"
    )
}

fun loadDataset() {
    val dataset = Dataset()
    val subset = dataset.training.filter { it.normality == Normality.positive && it.bodypart == Bodypart.HAND }
}

fun tryOverlay(file1: String, file2: String) {
    println(file1)
    println(file2)

    val base = ImageIO.read(File(file1))
    val sample = ImageIO.read(File(file2))
    DisplayImage(base)
    DisplayImage(sample)

    val overlay = ImageOverlayImpl()
    val transform = overlay.findBestOverlay(base, sample)

    DisplayImage(ImageOverlayImpl.overlay(base, sample, transform))
}
