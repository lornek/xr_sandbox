package dev.xrsandbox.player.smb

import com.hierynomus.msfscc.FileAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmbEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
)

object SmbBrowser {
    val videoExtensions = setOf("mkv", "mp4", "m4v", "mov", "webm", "avi", "ts", "m2ts", "wmv")

    /** Lists a folder: directories first, then playable video files, both sorted by name. */
    suspend fun list(config: SmbConfig, path: String): List<SmbEntry> = withContext(Dispatchers.IO) {
        SmbSession(config).use { s ->
            s.share.list(path.replace('/', '\\'))
                .filter { it.fileName != "." && it.fileName != ".." }
                .map {
                    val isDir = it.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L
                    SmbEntry(
                        name = it.fileName,
                        path = if (path.isEmpty()) it.fileName else "$path/${it.fileName}",
                        isDirectory = isDir,
                        size = it.endOfFile,
                    )
                }
                .filter { it.isDirectory || it.name.substringAfterLast('.', "").lowercase() in videoExtensions }
                .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
    }
}
