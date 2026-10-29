# xr_sandbox

Apps and utilities for Android XR devices such as the Samsung Galaxy XR.

## Projects

### `player/` – XR Player

Standalone video player that streams files straight from a machine on the LAN,
with no media-server middleman.

- **SMB (primary):** `SmbDataSource` is a Media3 `DataSource` built on
  [smbj](https://github.com/hierynomus/smbj). It does random-access reads, so
  seeking is instant and the original file is played untouched (direct play).
  Point it at the folder your Plex library already lives in: share that folder
  over SMB on the server and the same files serve both Plex and this app.
- **HTTP(S):** any URL ExoPlayer can play (progressive, HLS, DASH) via the
  "Play URL" box.
- **UI:** Compose. On Galaxy XR it runs as a normal resizable panel.

#### Build & run

Requires Android Studio (current stable) with JDK 17.

1. Open this folder in Android Studio and let Gradle sync.
2. Run the `player` configuration on the **Android XR emulator** or a headset.
   For a headset: enable developer mode, then `adb connect <headset-ip>:5555`
   (or pair over Wi-Fi) and run from Studio, or use
   `./gradlew :player:installDebug`.
3. In the app, enter the server host, share name, and credentials.

#### Roadmap

- [x] SMB browse + direct-play streaming
- [ ] Remember last position / resume
- [ ] Subtitle track selection
- [ ] Spatial video surface (flat / curved screen, 3D side-by-side and MV-HEVC,
      180/360) using Jetpack XR
- [ ] Cinema mode (environment dimming)
