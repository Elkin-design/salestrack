package org.salestrack.app.core.utils

import java.io.File

class JvmFileSaver : FileSaver {
    override suspend fun saveFile(fileName: String, bytes: ByteArray): String? {
        return try {
            val userHome = System.getProperty("user.home")
            val documentsFolder = File(userHome, "Documents")
            val salesTrackFolder = File(documentsFolder, "SalesTrack").apply {
                if (!exists()) mkdirs()
            }
            
            val file = File(salesTrackFolder, fileName)
            file.writeBytes(bytes)
            
            println("✅ Archivo guardado (JVM) en: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            println("❌ Error al guardar archivo (JVM): ${e.message}")
            null
        }
    }

    override suspend fun openFile(path: String, mimeType: String) {
        try {
            val file = File(path)
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            println("❌ Error al abrir archivo (JVM): ${e.message}")
        }
    }
}
