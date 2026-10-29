package dev.xrsandbox.player

import android.content.Context
import dev.xrsandbox.player.smb.SmbConfig

/** Persists the server connection. Plain SharedPreferences is fine for a LAN-only sandbox app. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("server", Context.MODE_PRIVATE)

    fun load() = SmbConfig(
        host = prefs.getString("host", "").orEmpty(),
        share = prefs.getString("share", "").orEmpty(),
        username = prefs.getString("username", "").orEmpty(),
        password = prefs.getString("password", "").orEmpty(),
        domain = prefs.getString("domain", "").orEmpty(),
        rootPath = prefs.getString("rootPath", "").orEmpty(),
    )

    fun save(c: SmbConfig) = prefs.edit()
        .putString("host", c.host.trim())
        .putString("share", c.share.trim())
        .putString("username", c.username)
        .putString("password", c.password)
        .putString("domain", c.domain)
        .putString("rootPath", c.rootPath.trim('/', '\\', ' '))
        .apply()
}
