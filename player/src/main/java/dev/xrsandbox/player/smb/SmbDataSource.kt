package dev.xrsandbox.player.smb

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.share.File
import java.io.IOException
import java.util.EnumSet

/**
 * Media3 data source that reads straight from an SMB share with random access,
 * so seeking works without any server-side transcoding or HTTP layer.
 * Expects URIs of the form smb://host/share/path/to/file.
 */
@UnstableApi
class SmbDataSource(private val config: SmbConfig) : BaseDataSource(/* isNetwork = */ true) {

    private var session: SmbSession? = null
    private var file: File? = null
    private var uri: Uri? = null
    private var position = 0L
    private var bytesRemaining = 0L
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)
        try {
            val segments = dataSpec.uri.pathSegments
            require(segments.size >= 2) { "Expected smb://host/share/path, got ${dataSpec.uri}" }
            val path = segments.drop(1).joinToString("\\")

            val s = SmbSession(config.copy(host = dataSpec.uri.host.orEmpty(), share = segments[0]))
            session = s
            val f = s.share.openFile(
                path,
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                SMB2CreateDisposition.FILE_OPEN,
                null,
            )
            file = f

            val size = f.fileInformation.standardInformation.endOfFile
            position = dataSpec.position
            bytesRemaining =
                if (dataSpec.length != C.LENGTH_UNSET.toLong()) dataSpec.length else size - position
            if (bytesRemaining < 0) throw IOException("Position ${dataSpec.position} beyond end of file ($size)")
        } catch (e: IOException) {
            close()
            throw e
        } catch (e: Exception) {
            close()
            throw IOException(e)
        }
        opened = true
        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT
        val toRead = minOf(bytesRemaining, length.toLong()).toInt()
        val n = try {
            file!!.read(buffer, position, offset, toRead)
        } catch (e: Exception) {
            throw IOException(e)
        }
        if (n < 0) return C.RESULT_END_OF_INPUT
        position += n
        bytesRemaining -= n
        bytesTransferred(n)
        return n
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        try {
            runCatching { file?.close() }
            session?.close()
        } finally {
            file = null
            session = null
            uri = null
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }

    class Factory(private val config: SmbConfig) : DataSource.Factory {
        override fun createDataSource(): DataSource = SmbDataSource(config)
    }
}
