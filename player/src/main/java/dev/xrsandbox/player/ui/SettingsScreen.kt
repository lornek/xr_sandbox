package dev.xrsandbox.player.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.xrsandbox.player.smb.SmbConfig

@Composable
fun SettingsScreen(
    initial: SmbConfig,
    onSave: (SmbConfig) -> Unit,
    onPlayUrl: (String) -> Unit,
) {
    var c by remember { mutableStateOf(initial) }
    var url by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("SMB server", style = MaterialTheme.typography.headlineSmall)
        Field("Host or IP (e.g. 192.168.1.20)", c.host) { c = c.copy(host = it) }
        Field("Share name (e.g. Media)", c.share) { c = c.copy(share = it) }
        Field("Start folder (optional)", c.rootPath) { c = c.copy(rootPath = it) }
        Field("Username (blank = guest)", c.username) { c = c.copy(username = it) }
        Field("Password", c.password, password = true) { c = c.copy(password = it) }
        Field("Domain (optional)", c.domain) { c = c.copy(domain = it) }
        Button(onClick = { onSave(c) }, enabled = c.isComplete) { Text("Save and browse") }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        Text("Or play a direct URL", style = MaterialTheme.typography.titleMedium)
        Field("http://host:port/file.mkv", url, keyboard = KeyboardType.Uri) { url = it }
        OutlinedButton(onClick = { onPlayUrl(url.trim()) }, enabled = url.isNotBlank()) { Text("Play URL") }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    password: Boolean = false,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) = OutlinedTextField(
    value = value,
    onValueChange = onChange,
    label = { Text(label) },
    singleLine = true,
    visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
    modifier = Modifier.fillMaxWidth(),
)
