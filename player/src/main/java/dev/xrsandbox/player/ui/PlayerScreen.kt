package dev.xrsandbox.player.ui

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import dev.xrsandbox.player.smb.SmbConfig
import dev.xrsandbox.player.smb.SmbDataSource

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(uri: String, config: SmbConfig) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaSource(buildMediaSource(context, Uri.parse(uri), config))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                this.player = player
            }
        },
        update = { it.player = player },
    )
}

/** smb:// goes through our random-access SMB source; everything else uses ExoPlayer's defaults. */
@OptIn(UnstableApi::class)
private fun buildMediaSource(context: android.content.Context, uri: Uri, config: SmbConfig): MediaSource {
    val item = MediaItem.fromUri(uri)
    return if (uri.scheme == SmbConfig.SCHEME) {
        ProgressiveMediaSource.Factory(SmbDataSource.Factory(config)).createMediaSource(item)
    } else {
        DefaultMediaSourceFactory(context).createMediaSource(item)
    }
}
