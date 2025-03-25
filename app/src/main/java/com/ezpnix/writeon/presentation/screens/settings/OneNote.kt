package com.ezpnix.writeon.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneNote(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalTextInputService.current

    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val iconColor = MaterialTheme.colorScheme.primary
    val caretColor = MaterialTheme.colorScheme.primary // Customize caret color

    var textState by remember { mutableStateOf(TextFieldValue(settingsViewModel.loadNote())) }
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    var showInfoPopup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("OneNote")
                        IconButton(onClick = { showInfoPopup = !showInfoPopup }) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = iconColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (undoStack.isNotEmpty()) {
                            redoStack.add(textState)
                            textState = undoStack.removeLast()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = "Undo",
                            tint = iconColor
                        )
                    }
                    IconButton(onClick = {
                        settingsViewModel.saveNote(textState.text)
                        Toast.makeText(context, "Note Saved!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "Save",
                            tint = iconColor
                        )
                    }
                    IconButton(onClick = {
                        if (redoStack.isNotEmpty()) {
                            undoStack.add(textState)
                            textState = redoStack.removeLast()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Redo,
                            contentDescription = "Redo",
                            tint = iconColor // Set icon color dynamically
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    BasicTextField(
                        value = textState,
                        onValueChange = {
                            undoStack.add(textState)
                            textState = it
                        },
                        textStyle = TextStyle(color = textColor),
                        modifier = Modifier.fillMaxSize(),
                        cursorBrush = SolidColor(caretColor),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (textState.text.isEmpty()) {
                                    Text(
                                        "Type here",
                                        style = TextStyle(color = placeholderColor)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                if (showInfoPopup) {
                    Popup(alignment = Alignment.TopCenter) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "About: Type in your thoughts onto one single note for easy access. Make sure to save and backup your stuff every new release.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
