package org.salestrack.app.core.utils

interface FileSaver {
    /**
     * Saves the given bytes to the local file system.
     * @param fileName The name of the file (e.g. "report.pdf")
     * @param bytes The data to save
     * @return The absolute path to the saved file, or null if failed
     */
    suspend fun saveFile(fileName: String, bytes: ByteArray): String?

    /**
     * Opens the file at the given path with the system's default application.
     * @param path The absolute path to the file
     * @param mimeType The MIME type of the file
     */
    suspend fun openFile(path: String, mimeType: String)
}
