package com.ezpnix.writeon.presentation.screens.settings.settings

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.DatabaseConst
import com.ezpnix.writeon.presentation.screens.edit.components.CustomTextField
import com.ezpnix.writeon.presentation.screens.settings.SettingsScaffold
import com.ezpnix.writeon.presentation.screens.settings.TxtBackupHelper
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.webDavDataStore: DataStore<Preferences> by preferencesDataStore(name = "webdav_prefs")

@Composable
@Suppress("DEPRECATION")
fun CloudScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val aead = remember {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, "webdav_keyset", "webdav_prefs")
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://webdav_master_key")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    val webdavUrlKey = stringPreferencesKey("webdav_url")
    val webdavUsernameKey = stringPreferencesKey("webdav_username")
    val webdavPasswordKey = stringPreferencesKey("webdav_password")

    var webdavUrl by remember { mutableStateOf("") }
    var webdavUsername by remember { mutableStateOf("") }
    var webdavPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        context.webDavDataStore.data.first().let { prefs ->
            webdavUrl = prefs[webdavUrlKey] ?: ""
            webdavUsername = prefs[webdavUsernameKey] ?: ""
            prefs[webdavPasswordKey]?.let { encryptedPassword ->
                webdavPassword = try {
                    String(aead.decrypt(Base64.decode(encryptedPassword, Base64.DEFAULT), byteArrayOf()))
                } catch (e: Exception) {
                    ""
                }
            } ?: ""
        }
    }

    var txtBackupUri by remember { mutableStateOf<Uri?>(null) }

    val exportTxtBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> txtBackupUri = uri }
    )

    val importTxtBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        TxtBackupHelper
                            .restoreNotesFromZipStream(context, uri)
                        Toast.makeText(context, "Imported! App will restart now.", Toast.LENGTH_LONG).show()
                        kotlinx.coroutines.delay(1000)
                        ProcessPhoenix.triggerRebirth(context)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to restore TXT backup: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    LaunchedEffect(txtBackupUri) {
        txtBackupUri?.let { uri ->
            TxtBackupHelper
                .writeNotesToZipStream(context, uri)
            Toast.makeText(context, "TXT Backup saved!", Toast.LENGTH_SHORT).show()
            txtBackupUri = null
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> if (uri != null) settingsViewModel.onExportBackup(uri, context) }
    )

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        settingsViewModel.onImportBackup(uri, context)
                        Toast.makeText(context, "Restored! App will restart now.", Toast.LENGTH_LONG).show()
                        kotlinx.coroutines.delay(1000)
                        ProcessPhoenix.triggerRebirth(context)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to restore backup: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    var showBackupPasswordPrompt by remember { mutableStateOf(false) }
    var showRestorePasswordPrompt by remember { mutableStateOf(false) }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = "Backup & Restore",
        onBackNavClicked = { navController.navigateUp() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠️ Experimental Feature",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "This feature is still a work in progress. Manual backup or restore is still recommended for now.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("WebDAV Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = webdavUrl,
                onValueChange = { webdavUrl = it },
                label = { Text("WebDAV URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = webdavUsername,
                onValueChange = { webdavUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = webdavPassword,
                onValueChange = { webdavPassword = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                coroutineScope.launch {
                    context.webDavDataStore.edit { prefs ->
                        prefs[webdavUrlKey] = webdavUrl
                        prefs[webdavUsernameKey] = webdavUsername
                        if (webdavPassword.isNotBlank()) {
                            val encryptedPassword = Base64.encodeToString(
                                aead.encrypt(webdavPassword.toByteArray(), byteArrayOf()),
                                Base64.DEFAULT
                            )
                            prefs[webdavPasswordKey] = encryptedPassword
                        } else {
                            prefs.remove(webdavPasswordKey)
                        }
                    }
                    Toast.makeText(context, "WebDAV credentials saved!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save WebDAV Info")
            }

            Spacer(Modifier.height(16.dp))
            Text("Backup Options", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.Cloud,
                title = "Backup to WebDAV",
                description = "Upload your encrypted notes to a WebDAV server (e.g., Nextcloud, Koofr)",
                primaryButtonText = "Backup to cloud",
                secondaryButtonText = "Restore",
                onPrimaryClick = {
                    if (webdavUrl.isBlank() || webdavUsername.isBlank() || webdavPassword.isBlank()) {
                        Toast.makeText(context, "Enter WebDAV info first", Toast.LENGTH_LONG).show()
                    } else {
                        coroutineScope.launch {
                            context.webDavDataStore.edit { prefs ->
                                prefs[webdavUrlKey] = webdavUrl
                                prefs[webdavUsernameKey] = webdavUsername
                                if (webdavPassword.isNotBlank()) {
                                    val encryptedPassword = Base64.encodeToString(
                                        aead.encrypt(webdavPassword.toByteArray(), byteArrayOf()),
                                        Base64.DEFAULT
                                    )
                                    prefs[webdavPasswordKey] = encryptedPassword
                                }
                            }
                            workManager.enqueue(
                                OneTimeWorkRequestBuilder<com.ezpnix.writeon.presentation.screens.settings.WebDAVBackupWorker>()
                                    .build()
                            )
                            Toast.makeText(context, "WebDAV backup started", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSecondaryClick = {
                    if (webdavUrl.isBlank() || webdavUsername.isBlank() || webdavPassword.isBlank()) {
                        Toast.makeText(context, "Enter WebDAV info first", Toast.LENGTH_LONG).show()
                    } else {
                        coroutineScope.launch {
                            context.webDavDataStore.edit { prefs ->
                                prefs[webdavUrlKey] = webdavUrl
                                prefs[webdavUsernameKey] = webdavUsername
                                if (webdavPassword.isNotBlank()) {
                                    val encryptedPassword = Base64.encodeToString(
                                        aead.encrypt(webdavPassword.toByteArray(), byteArrayOf()),
                                        Base64.DEFAULT
                                    )
                                    prefs[webdavPasswordKey] = encryptedPassword
                                }
                            }
                            workManager.enqueue(
                                OneTimeWorkRequestBuilder<com.ezpnix.writeon.presentation.screens.settings.WebDAVRestoreWorker>()
                                    .build()
                            )
                            Toast.makeText(context, "WebDAV restore started", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.ArrowCircleUp,
                title = stringResource(id = R.string.database_backup),
                description = stringResource(id = R.string.backup_description),
                primaryButtonText = "Backup",
                secondaryButtonText = "Restore",
                onPrimaryClick = {
                    if (settingsViewModel.settings.value.encryptBackup) {
                        showBackupPasswordPrompt = true
                    } else {
                        exportBackupLauncher.launch("${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip")
                    }
                },
                onSecondaryClick = {
                    if (settingsViewModel.settings.value.encryptBackup) {
                        showRestorePasswordPrompt = true
                    } else {
                        settingsViewModel.password = null
                        importBackupLauncher.launch(arrayOf("application/zip"))
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.Description,
                title = "Backup as TXT",
                description = "Export notes as plain TXT files without encryption all compiled into one zip file",
                primaryButtonText = "Export",
                secondaryButtonText = "Import",
                onPrimaryClick = {
                    exportTxtBackupLauncher.launch("notes-backup-${currentDateTime()}.zip")
                },
                onSecondaryClick = {
                    importTxtBackupLauncher.launch(arrayOf("application/zip"))
                }
            )

            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.Backup,
                title = stringResource(id = R.string.auto_backup),
                description = stringResource(id = R.string.auto_backup_description),
                primaryButtonText = null,
                onPrimaryClick = {},
                switchState = settingsViewModel.settings.value.autoBackupEnabled,
                onSwitchToggle = { isEnabled ->
                    settingsViewModel.update(settingsViewModel.settings.value.copy(autoBackupEnabled = isEnabled))
                    val msg = if (isEnabled) "Enabled - Everyday Backup" else "Disabled Auto-Backup"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showBackupPasswordPrompt) {
        PasswordPrompt(
            context = context,
            text = stringResource(id = R.string.database_backup),
            settingsViewModel = settingsViewModel,
            onExit = { password ->
                showBackupPasswordPrompt = false
                if (password != null) {
                    settingsViewModel.password = password.text
                    exportBackupLauncher.launch("${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip")
                }
            }
        )
    }

    if (showRestorePasswordPrompt) {
        PasswordPrompt(
            context = context,
            text = stringResource(id = R.string.database_restore),
            settingsViewModel = settingsViewModel,
            onExit = { password ->
                showRestorePasswordPrompt = false
                if (password != null) {
                    settingsViewModel.password = password.text
                    importBackupLauncher.launch(arrayOf("application/zip"))
                }
            }
        )
    }
}

@Composable
fun BackupOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    primaryButtonText: String? = null,
    secondaryButtonText: String? = null,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: (() -> Unit)? = null,
    switchState: Boolean? = null,
    onSwitchToggle: ((Boolean) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(description, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Row {
                when {
                    switchState != null && onSwitchToggle != null -> {
                        Switch(
                            checked = switchState,
                            onCheckedChange = onSwitchToggle,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    primaryButtonText != null -> {
                        Button(onClick = onPrimaryClick) {
                            Text(primaryButtonText)
                        }
                    }
                }
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onSecondaryClick) {
                        Text(secondaryButtonText)
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordPrompt(
    context: Context,
    text: String,
    settingsViewModel: SettingsViewModel,
    onExit: (TextFieldValue?) -> Unit,
) {
    var password by remember { mutableStateOf(TextFieldValue("")) }
    Dialog(
        onDismissRequest = { onExit(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = shapeManager(
                        isBoth = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                hideContent = true,
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(id = R.string.password_prompt),
                modifier = Modifier.padding(12.dp)
            )
            Button(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.End)
                    .padding(12.dp),
                onClick = {
                    if (password.text.isNotBlank()) {
                        onExit(password)
                    } else {
                        Toast.makeText(context, R.string.invalid_input, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(text)
            }
        }
    }
}