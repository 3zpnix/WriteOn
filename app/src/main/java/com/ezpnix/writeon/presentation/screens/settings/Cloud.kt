package com.ezpnix.writeon.presentation.screens.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.DatabaseConst
import com.ezpnix.writeon.presentation.screens.edit.components.CustomTextField
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Composable
fun CloudScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    var txtBackupUri by remember { mutableStateOf<Uri?>(null) }

    val exportTxtBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> txtBackupUri = uri }
    )

    LaunchedEffect(txtBackupUri) {
        txtBackupUri?.let { uri ->
            TxtBackupHelper.writeNotesToZipStream(context, uri)
            Toast.makeText(context, "TXT Backup saved!", Toast.LENGTH_SHORT).show()
            txtBackupUri = null // reset after use
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/.zip"),
        onResult = { uri ->
            if (uri != null) settingsViewModel.onExportBackup(uri, context)
        }
    )
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) settingsViewModel.onImportBackup(uri, context)
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
                        text = "This feature is still experimental. The developer is still testing it out and needs feedback from the users as well. Manual backup/restore is still recommended for now.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                "webdav_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var webdavUrl by remember { mutableStateOf(prefs.getString("webdav_url", "") ?: "") }
            var webdavUsername by remember { mutableStateOf(prefs.getString("webdav_username", "") ?: "") }
            var webdavPassword by remember { mutableStateOf(prefs.getString("webdav_password", "") ?: "") }

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
                prefs.edit()
                    .putString("webdav_url", webdavUrl)
                    .putString("webdav_username", webdavUsername)
                    .putString("webdav_password", webdavPassword)
                    .apply()
                Toast.makeText(context, "WebDAV credentials saved!", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(
                            context,
                            "Please enter WebDAV URL, username, and password before starting backup.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        prefs.edit()
                            .putString("webdav_url", webdavUrl)
                            .putString("webdav_username", webdavUsername)
                            .putString("webdav_password", webdavPassword)
                            .apply()

                        workManager.enqueue(OneTimeWorkRequestBuilder<WebDAVBackupWorker>().build())
                        Toast.makeText(context, "WebDAV backup started", Toast.LENGTH_SHORT).show()
                    }
                },
                onSecondaryClick = {
                    if (webdavUrl.isBlank() || webdavUsername.isBlank() || webdavPassword.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please enter WebDAV URL, username, and password before starting restore.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        prefs.edit()
                            .putString("webdav_url", webdavUrl)
                            .putString("webdav_username", webdavUsername)
                            .putString("webdav_password", webdavPassword)
                            .apply()

                        workManager.enqueue(OneTimeWorkRequestBuilder<WebDAVRestoreWorker>().build())
                        Toast.makeText(context, "WebDAV restore started", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.ArrowCircleUp,
                title = stringResource(id = R.string.manual_backup),
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

            Spacer(Modifier.height(16.dp))

            BackupOptionCard(
                icon = Icons.Rounded.Description,
                title = "Backup as TXT",
                description = "This will export notes to plain txt file format without encryption. This does not have a restore to app feature yet",
                primaryButtonText = "Export",
                onPrimaryClick = {
                    exportTxtBackupLauncher.launch("notes-backup-${currentDateTime()}.zip")
                }
            )
        }
    }

    if (showBackupPasswordPrompt) {
        PasswordPrompt(
            context = context,
            text = stringResource(id = R.string.manual_backup),
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
            text = stringResource(id = R.string.manual_restore),
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
    onSwitchToggle: ((Boolean) -> Unit)? = null,
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

fun currentDateTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ms")
    return currentDateTime.format(formatter)
}

@Composable
fun BackupOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    primaryButtonText: String,
    secondaryButtonText: String? = null,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Button(onClick = onPrimaryClick) {
                    Text(primaryButtonText)
                }
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onSecondaryClick) {
                        Text(secondaryButtonText)
                    }
                }
            }
        }
    }
}