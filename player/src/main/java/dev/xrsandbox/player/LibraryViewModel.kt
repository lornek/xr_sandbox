package dev.xrsandbox.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.xrsandbox.player.smb.SmbBrowser
import dev.xrsandbox.player.smb.SmbConfig
import dev.xrsandbox.player.smb.SmbEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Settings : Screen
    data class Browse(val path: String) : Screen
    data class Play(val title: String, val uri: String) : Screen
}

data class BrowseState(
    val loading: Boolean = false,
    val entries: List<SmbEntry> = emptyList(),
    val error: String? = null,
)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsStore(app)

    var config: SmbConfig = store.load()
        private set

    private val _screen = MutableStateFlow<Screen>(
        if (config.isComplete) Screen.Browse(config.rootPath) else Screen.Settings,
    )
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _browse = MutableStateFlow(BrowseState())
    val browse: StateFlow<BrowseState> = _browse.asStateFlow()

    init {
        (screen.value as? Screen.Browse)?.let { load(it.path) }
    }

    fun saveSettings(c: SmbConfig) {
        config = c
        store.save(c)
        open(c.rootPath)
    }

    fun showSettings() { _screen.value = Screen.Settings }

    fun open(path: String) {
        _screen.value = Screen.Browse(path)
        load(path)
    }

    private var returnTo: Screen = Screen.Settings

    fun play(entry: SmbEntry) {
        returnTo = _screen.value
        _screen.value = Screen.Play(entry.name, config.uriFor(entry.path).toString())
    }

    /** Plays any URL ExoPlayer understands (http, https, hls, dash), bypassing SMB. */
    fun playUrl(url: String) {
        returnTo = _screen.value
        _screen.value = Screen.Play(url.substringAfterLast('/'), url)
    }

    /** Returns false when already at the top so the activity can finish. */
    fun back(): Boolean {
        when (val s = _screen.value) {
            is Screen.Play -> when (val r = returnTo) {
                is Screen.Browse -> open(r.path)
                else -> showSettings()
            }
            is Screen.Browse -> {
                if (s.path == config.rootPath || s.path.isEmpty()) return false
                open(parentOf(s.path))
            }
            Screen.Settings -> if (config.isComplete) open(config.rootPath) else return false
        }
        return true
    }

    private fun parentOf(path: String) = path.substringBeforeLast('/', "")

    private fun load(path: String) {
        _browse.value = BrowseState(loading = true)
        viewModelScope.launch {
            _browse.value = try {
                BrowseState(entries = SmbBrowser.list(config, path))
            } catch (e: Exception) {
                BrowseState(error = e.message ?: e.javaClass.simpleName)
            }
        }
    }
}
