package dev.xrsandbox.player.smb

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.Closeable

/** One authenticated connection to a single share. Close it when done. */
class SmbSession(config: SmbConfig) : Closeable {
    private val client = SMBClient()
    private val connection: Connection = client.connect(config.host)
    private val session: Session = connection.authenticate(
        if (config.username.isBlank()) {
            AuthenticationContext.guest()
        } else {
            AuthenticationContext(config.username, config.password.toCharArray(), config.domain)
        },
    )
    val share: DiskShare = session.connectShare(config.share) as DiskShare

    override fun close() {
        runCatching { share.close() }
        runCatching { session.close() }
        runCatching { connection.close() }
        runCatching { client.close() }
    }
}
