package dev.xrsandbox.player.smb

import android.net.Uri

data class SmbConfig(
    val host: String = "",
    val share: String = "",
    val username: String = "",
    val password: String = "",
    val domain: String = "",
    /** Folder inside the share where browsing starts, e.g. "Movies". */
    val rootPath: String = "",
) {
    val isComplete get() = host.isNotBlank() && share.isNotBlank()

    /** Builds smb://host/share/path, the form [SmbDataSource] parses. */
    fun uriFor(path: String): Uri = Uri.Builder()
        .scheme(SCHEME)
        .authority(host)
        .appendPath(share)
        .apply { path.split('/').filter { it.isNotEmpty() }.forEach(::appendPath) }
        .build()

    companion object {
        const val SCHEME = "smb"
    }
}
