package org.salestrack.app.core.utils

import android.os.Environment
import java.io.File
import org.salestrack.app.core.di.androidContextStore

class AndroidFileSaver : FileSaver {
    override suspend fun saveFile(fileName: String, bytes: ByteArray): String? {
        return try {
            val context = androidContextStore ?: return null
            
            // Usamos el directorio de descargas de la app para evitar problemas de permisos
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (directory != null && !directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, fileName)
            file.writeBytes(bytes)
            
            println("✅ Archivo guardado (Android) en: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            println("❌ Error al guardar archivo (Android): ${e.message}")
            null
        }
    }

    override suspend fun openFile(path: String, mimeType: String) {
        try {
            val context = androidContextStore ?: return
            val file = File(path)
            if (!file.exists()) return

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            println("❌ Error al abrir archivo (Android): ${e.message}")
        }
    }
}
