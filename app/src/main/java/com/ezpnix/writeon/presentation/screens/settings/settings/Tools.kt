package com.ezpnix.writeon.presentation.screens.settings.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Abc
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.presentation.screens.settings.SettingsScaffold
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.widgets.ActionType
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingsBox

@Composable
fun ToolsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    settingsViewModel.noteUseCase.observe()
    var showSearchDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var dynamicPlaceholderInput by remember { mutableStateOf("Enter text") }
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.tools),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    title = if (settingsViewModel.settings.value.sortDescending) stringResource(id = R.string.sort_descending) else stringResource(
                        id = R.string.sort_ascending
                    ),
                    description = stringResource(id = R.string.sort_description),
                    icon = Icons.AutoMirrored.Rounded.Sort,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.sortDescending,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                sortDescending = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.always_edit),
                    description = stringResource(id = R.string.always_edit_description),
                    icon = Icons.Rounded.Edit,
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    variable = settingsViewModel.settings.value.editMode,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(settingsViewModel.settings.value.copy(editMode = isEnabled))

                        val message = if (isEnabled) {
                            "<Edit Mode>"
                        } else {
                            "<View Mode>"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.markdown),
                    description = stringResource(id = R.string.markdown_description),
                    icon = Icons.Rounded.Style,
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    variable = settingsViewModel.settings.value.isMarkdownEnabled,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                isMarkdownEnabled = isEnabled
                            )
                        )
                        val message = if (isEnabled) {
                            "Enabled (Recommended)"
                        } else {
                            "Disabled"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.show_only_title),
                    description = stringResource(id = R.string.show_only_title_description),
                    icon = Icons.Rounded.Architecture,
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    variable = settingsViewModel.settings.value.showOnlyTitle,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                showOnlyTitle = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    isBig = true,
                    title = stringResource(id = R.string.font_size),
                    description = stringResource(id = R.string.enter_font_size_description),
                    icon = Icons.Rounded.TextFields,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    customAction = {
                        showFontSizeDialog = true
                    }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.enter_placeholder),
                    description = stringResource(id = R.string.enter_placeholder_description),
                    icon = Icons.Rounded.AutoFixHigh,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = false
                    ),
                    customAction = {
                        showSearchDialog = true
                    }
                )
            }
            item {
                SettingsBox(
                    title = "Auto-hide visibility button",
                    description = "Hides the button when the keyboard is visible while editing a note.",
                    icon = Icons.Rounded.VisibilityOff,
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    variable = settingsViewModel.settings.value.hideVisibilityButtonWhenKeyboard,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                hideVisibilityButtonWhenKeyboard = it
                            )
                        )
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
                    radius = shapeManager(
                        isBoth = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    customAction = { navController.navigateUp() }
                )
            }
        }
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = true },
                title = { Text("Example: Hello!") },
                text = {
                    TextField(
                        value = dynamicPlaceholderInput,
                        onValueChange = { newText ->
                            dynamicPlaceholderInput = newText
                        },
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        settingsViewModel.updatePlaceholder(dynamicPlaceholderInput)
                        repeat(2) { navController.popBackStack() }
                        Toast.makeText(context, "Text updated!", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        repeat(2) { navController.popBackStack() }
                        Toast.makeText(context, "Text updated!", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        Text("Exit")
                    }
                }
            )
        }
        if (showFontSizeDialog) {
            AlertDialog(
                onDismissRequest = { showFontSizeDialog = true },
                title = { Text(stringResource(id = R.string.select_font_size)) },
                text = {
                    Column {
                        val fontSizeOptions =
                            listOf(12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f)

                        fontSizeOptions.forEach { size ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showFontSizeDialog = false
                                    }
                                    .padding(1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = settingsViewModel.settings.value.fontSize == size,
                                    onCheckedChange = { settingsViewModel.updateFontSize(size) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${size.toInt()} sp", fontSize = size.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showFontSizeDialog = false
                        repeat(2) { navController.popBackStack() }
                        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        Text(stringResource(id = R.string.save_and_exit))
                    }
                }
            )
        }
    }
}