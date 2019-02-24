package uk.ac.cam.cl.bravo.dataset

import uk.ac.cam.cl.bravo.classify.BodypartViewClassifierImpl
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
        private const val VIEW_CLUSTERING_OUTPUT_DIR = "python/view_clustering/output/"

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
                path to ImageSample(path, getBodypartViewOf(path))
            } catch (e: IllegalArgumentException) {
                System.err.println(e.message)
                null
            }
        }.toMap()
    }

    private val bodypartViews: Map<String, BodypartView>

    private fun getBodypartViewOf(path: String): BodypartView {
        // This throws an exception if the path is not found in the map
        try {
            val key = path.removePrefix("MURA-v1.1/").substringBeforeLast('_') + "/" + path.substringAfterLast('_')
            return bodypartViews.getValue(key)
        } catch (e: NoSuchElementException) {
            throw RuntimeException("The image $path has no BodypartView assigned in $VIEW_CLUSTERING_OUTPUT_DIR")
        }
    }

    init {
        checkDatasetFolderExists()

        bodypartViews = BodypartViewClassifierImpl.buildBodyPartViews(VIEW_CLUSTERING_OUTPUT_DIR)

        // load list of all images
        training = loadImageSamples(CSV_TRAIN_IMAGE_PATHS)
        validation = loadImageSamples(CSV_VALID_IMAGE_PATHS)
        println("${training.size} training samples loaded.")
    }
}
