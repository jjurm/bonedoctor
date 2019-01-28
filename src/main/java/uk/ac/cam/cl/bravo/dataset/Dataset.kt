package uk.ac.cam.cl.bravo.dataset

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * Loads the MURA dataset.
 * Assumes that all images are in the 'dataset'.
 */
class Dataset @Throws(IOException::class) constructor() {

    companion object {
        private const val DIR = "MURA-v1.1/"
        private const val CSV_TRAIN_IMAGE_PATHS = DIR + "train_image_paths.csv"
        private const val CSV_TRAIN_LABELED_STUDIES = DIR + "train_labeled_studies.csv"
        private const val CSV_VALID_IMAGE_PATHS = DIR + "valid_image_paths.csv"
        private const val CSV_VALID_LABELED_STUDIES = DIR + "valid_labeled_studies.csv"

    }

    val training: List<ImageSample>
    val validation: List<ImageSample>

    private fun checkDatasetFolderExists() {
        if (!Files.isDirectory(Paths.get(DIR))) {
            throw RuntimeException(
                "Folder '$DIR' is missing. Please copy the MURA dataset (train, valid folders " +
                        "and the CSV files) into the '$DIR' directory in the project root."
            )
        }
    }

    @Throws(IOException::class)
    private fun loadPaths(filename: String): List<String> {
        return BufferedReader(FileReader(filename)).use { it.lines().toList() }
    }

    private fun loadImageSamples(csvFilename: String): List<ImageSample> {
        return loadPaths(csvFilename).mapNotNull { path ->
            try {
                ImageSample(path, getPatient(path), getBodypart(path), getNormality(path))
            } catch (e: IllegalArgumentException) {
                System.err.println(e.message)
                null
            }
        }
    }

    private val patientRegex = Regex("""patient(\d+)""")
    private val bodypartRegex = Regex("""XR_(\w+)""")
    private val normalityRegex = Regex("""study\d+_(\w+)""")

    private fun getPatient(path: String): Int =
        patientRegex.find(path)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: throw IllegalArgumentException("Cannot determine patient number of $path")

    private fun getBodypart(path: String): Bodypart =
        bodypartRegex.find(path)?.groupValues?.getOrNull(1)?.let { Bodypart.valueOf(it) }
            ?: throw IllegalArgumentException("Cannot determine body part of $path")

    private fun getNormality(path: String): Normality =
        normalityRegex.find(path)?.groupValues?.getOrNull(1)?.let { Normality.valueOf(it) }
            ?: throw IllegalArgumentException("Cannot determine normality of $path")

    init {
        checkDatasetFolderExists()
        training = loadImageSamples(CSV_TRAIN_IMAGE_PATHS)
        validation = loadImageSamples(CSV_VALID_IMAGE_PATHS)
        println("${training.size} training samples loaded.")
    }
}
