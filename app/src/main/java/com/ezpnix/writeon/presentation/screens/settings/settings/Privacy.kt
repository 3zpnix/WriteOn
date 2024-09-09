package com.ezpnix.writeon.presentation.screens.settings.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.DatabaseConst
import com.ezpnix.writeon.presentation.screens.edit.components.CustomTextField
import com.ezpnix.writeon.presentation.screens.settings.SettingsScaffold
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.widgets.ActionType
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingsBox
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PrivacyScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
val context = LocalContext.current

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

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.privacy),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    title = stringResource(id = R.string.biometric_authentication),
                    description = stringResource(id = R.string.biometric_authentication_description),
                    icon = Icons.Filled.Fingerprint,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.isBiometricEnabled,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(settingsViewModel.settings.value.copy(isBiometricEnabled = isEnabled))

                        val message = if (isEnabled) {
                            "Activated"
                        } else {
                            "Disabled"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.screen_protection),
                    description = stringResource(id = R.string.screen_protection_description),
                    icon = Icons.Filled.RemoveRedEye,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.screenProtection,
                    switchEnabled = { isEnabled -> settingsViewModel.update(settingsViewModel.settings.value.copy(screenProtection = isEnabled))

                        val message = if (isEnabled) {
                            "Screen Protected"
                        } else {
                            "Screen Unprotected"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
//            NOT USED FOR NOW
//            item {
//                SettingsBox(
//                    title = stringResource(id = R.string.security),
//                    description = stringResource(id = R.string.vault_description),
//                    icon = Icons.Rounded.Security,
//                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
//                    actionType = ActionType.SWITCH,
//                    variable = settingsViewModel.settings.value.vaultSettingEnabled,
//                    switchEnabled = { isEnabled ->
//                        settingsViewModel.update(settingsViewModel.settings.value.copy(vaultSettingEnabled = isEnabled))
//
//                        val message = if (isEnabled) {
//                            "Using Advanced Protection"
//                        } else {
//                            "Using Basic Protection"
//                        }
//                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                    }
//                )
//            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.encrypt_database),
                    description = stringResource(id = R.string.encrypt_database_description),
                    icon = Icons.Rounded.Security,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    variable = settingsViewModel.settings.value.encryptBackup,
                    actionType = ActionType.SWITCH,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(settingsViewModel.settings.value.copy(encryptBackup = isEnabled))

                        val message = if (isEnabled) {
                            "Database Encryption Enabled"
                        } else {
                            "Database Encryption Disabled"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SettingsBox(
                    title = stringResource(id = R.string.auto_backup),
                    description = stringResource(id = R.string.auto_backup_description),
                    icon = Icons.Rounded.Backup,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.autoBackupEnabled,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(settingsViewModel.settings.value.copy(autoBackupEnabled = isEnabled))

                        val message = if (isEnabled) {
                            "Backup Successful"
                        } else {
                            "Disabled Auto Backup"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.backup),
                    description = stringResource(id = R.string.backup_description),
                    icon = Icons.Rounded.ArrowCircleUp,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.CUSTOM,
                    customAction = {onExit ->
                        if (settingsViewModel.settings.value.encryptBackup) {
                            PasswordPrompt(
                                context = context,
                                text = stringResource(id = R.string.backup),
                                settingsViewModel = settingsViewModel,
                                onExit = { password ->
                                    if (password != null) {
                                        settingsViewModel.password = password.text
                                    }
                                    onExit()
                                },
                                onBackup = {
                                    exportBackupLauncher.launch("${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip")
                                }
                            )
                        }
                        else {
                            LaunchedEffect(true) {
                                exportBackupLauncher.launch("${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip")
                            }
                        }
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.restore),
                    description = stringResource(id = R.string.restore_description),
                    icon = Icons.Rounded.ArrowCircleDown,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        if (settingsViewModel.settings.value.encryptBackup) {
                            PasswordPrompt(
                                context = context,
                                text = stringResource(id = R.string.restore),
                                settingsViewModel = settingsViewModel,
                                onExit = { password ->
                                    if (password != null) {
                                        settingsViewModel.password = password.text
                                    }
                                    onExit()
                                },
                                onBackup = {
                                    importBackupLauncher.launch(arrayOf("application/zip"))
                                }
                            )
                        }
                        else {
                            LaunchedEffect(true) {
                                settingsViewModel.password = null
                                importBackupLauncher.launch(arrayOf("application/zip"))
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    isBig = true,
                    title = stringResource(id = R.string.homepage),
                    icon = Icons.Rounded.Home,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(isBoth = true, radius = settingsViewModel.settings.value.cornerRadius),
                    customAction = { navController.navigateUp() }
                )
            }
        }
    }
}

fun currentDateTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ms")
    val formattedDateTime = currentDateTime.format(formatter)

    return formattedDateTime
}

@Composable
fun PasswordPrompt(context: Context, text: String, settingsViewModel: SettingsViewModel, onExit: (TextFieldValue?) -> Unit, onBackup: () -> Unit = {}) {
    var password by remember { mutableStateOf(TextFieldValue("")) }
    Dialog(
        onDismissRequest = { onExit(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        LazyColumn {
            item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.2f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = shapeManager(
                                isBoth = true,
                                radius = settingsViewModel.settings.value.cornerRadius
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = shapeManager(
                                    isBoth = true,
                                    radius = settingsViewModel.settings.value.cornerRadius
                                )
                            )
                    ) {
                        CustomTextField(
                            hideContent = true,
                            value = password,
                            onValueChange = { password = it },
                            placeholder = stringResource(id = R.string.password_prompt)
                        )
                    }
                    Button(
                        modifier = Modifier
                            .padding(12.dp)
                            .wrapContentWidth()
                            .align(Alignment.End),
                        onClick = {
                            if (password.text.isNotBlank()) {
                                onExit(password)
                                onBackup()
                            } else {
                                Toast.makeText(context, R.string.invalid_input, Toast.LENGTH_SHORT).show()
                            }
                        },
                        content = {
                            Text(text)
                        }
                    )
                }
            }
        }
    }
}
