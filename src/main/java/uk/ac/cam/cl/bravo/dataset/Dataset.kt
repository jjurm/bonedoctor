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
        const val DIR = "MURA-v1.1/"
        const val DIR_PREPROCESSED = "MURA-preprocessed/"
        private const val CSV_TRAIN_IMAGE_PATHS = DIR + "train_image_paths.csv"
        private const val CSV_VALID_IMAGE_PATHS = DIR + "valid_image_paths.csv"

    }
    
    val training: Map<String, ImageSample>
    val validation: Map<String, ImageSample>

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

    private fun loadImageSamples(csvFilename: String): Map<String, ImageSample> {
        return loadPaths(csvFilename).mapNotNull { path ->
            try {
                path to ImageSample(path)
            } catch (e: IllegalArgumentException) {
                System.err.println(e.message)
                null
            }
        }.toMap()
    }

    private fun getBodypartViewOf(path: String): BodypartView {
        TODO()
    }

    init {
        checkDatasetFolderExists()
        training = loadImageSamples(CSV_TRAIN_IMAGE_PATHS)
        validation = loadImageSamples(CSV_VALID_IMAGE_PATHS)
        println("${training.size} training samples loaded.")
    }
}
