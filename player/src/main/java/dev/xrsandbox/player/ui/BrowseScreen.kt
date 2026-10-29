package dev.xrsandbox.player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.xrsandbox.player.BrowseState
import dev.xrsandbox.player.smb.SmbEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    path: String,
    state: BrowseState,
    onOpen: (SmbEntry) -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (path.isEmpty()) "/" else "/$path") },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings") } },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    "Couldn't load folder:\n${state.error}",
                    Modifier.align(Alignment.Center).padding(32.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                state.entries.isEmpty() -> Text("No folders or videos here", Modifier.align(Alignment.Center))
                else -> LazyColumn {
                    items(state.entries, key = { it.path }) { e ->
                        ListItem(
                            headlineContent = { Text(e.name) },
                            supportingContent = if (e.isDirectory) null else ({ Text(formatSize(e.size)) }),
                            leadingContent = {
                                Icon(if (e.isDirectory) Icons.Default.Folder else Icons.Default.Movie, null)
                            },
                            modifier = Modifier.clickable { onOpen(e) },
                        )
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1L shl 30 -> "%.1f GB".format(bytes / (1L shl 30).toDouble())
    bytes >= 1L shl 20 -> "%.0f MB".format(bytes / (1L shl 20).toDouble())
    else -> "%d KB".format(bytes shr 10)
}
