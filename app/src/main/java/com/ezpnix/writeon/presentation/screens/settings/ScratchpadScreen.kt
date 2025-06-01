package com.ezpnix.writeon.presentation.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchpadScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val originalNote = remember { settingsViewModel.loadNote() }
    var textState by remember { mutableStateOf(TextFieldValue(originalNote)) }
    val hasChanges = textState.text != originalNote

    val saveToFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(textState.text.toByteArray())
                    }
                    Toast.makeText(context, "Text saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    var showInfoPopup by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Scratchpad")
                        IconButton(onClick = { showInfoPopup = !showInfoPopup }) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showSaveDialog = true
                        else navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (undoStack.isNotEmpty()) {
                            redoStack.add(textState)
                            textState = undoStack.removeLast()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()
                        saveToFileLauncher.launch("note.txt")
                    }) {
                        Icon(Icons.Rounded.AddComment, contentDescription = "Save as TXT", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        if (redoStack.isNotEmpty()) {
                            undoStack.add(textState)
                            textState = redoStack.removeLast()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                ) {
                    BasicTextField(
                        value = textState,
                        onValueChange = {
                            undoStack.add(textState)
                            textState = it
                        },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxSize(),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (textState.text.isEmpty()) {
                                    Text(
                                        "Type here...",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                inner()
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
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Text(
                                "About: This section is where you can type anything in just one single area. Warning: Data will be deleted if app restarts or is uninstalled.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                if (showSaveDialog) {
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = false },
                        title = { Text("Save Changes?") },
                        text = { Text("You have unsaved changes. Save before leaving?") },
                        confirmButton = {
                            TextButton(onClick = {
                                settingsViewModel.saveNote(textState.text)
                                Toast.makeText(context, "Note Saved!", Toast.LENGTH_SHORT).show()
                                showSaveDialog = false
                                navController.navigateUp()
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showSaveDialog = false
                                navController.navigateUp()
                            }) {
                                Text("Discard")
                            }
                        }
                    )
                }
            }
        }
    )
}
