package dev.xrsandbox.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.xrsandbox.player.ui.BrowseScreen
import dev.xrsandbox.player.ui.PlayerScreen
import dev.xrsandbox.player.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private val vm: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!vm.back()) finish()
            }
        })

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface {
                    val screen by vm.screen.collectAsState()
                    val browse by vm.browse.collectAsState()
                    when (val s = screen) {
                        Screen.Settings -> SettingsScreen(vm.config, vm::saveSettings, vm::playUrl)
                        is Screen.Browse -> BrowseScreen(
                            path = s.path,
                            state = browse,
                            onOpen = { if (it.isDirectory) vm.open(it.path) else vm.play(it) },
                            onSettings = vm::showSettings,
                        )
                        is Screen.Play -> PlayerScreen(s.uri, vm.config)
                    }
                }
            }
        }
    }
}
