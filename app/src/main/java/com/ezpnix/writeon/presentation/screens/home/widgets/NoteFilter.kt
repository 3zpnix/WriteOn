package com.ezpnix.writeon.presentation.screens.home.widgets

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.screens.edit.components.CustomTextField
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.PasswordPrompt
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager

@Composable
fun NoteFilter(
    settingsViewModel: SettingsViewModel,
    containerColor: Color,
    onNoteClicked: (Int) -> Unit,
    shape: RoundedCornerShape,
    notes: List<Note>,
    searchText: String? = null,
    selectedNotes: MutableList<Note> = mutableListOf(),
    viewMode: Boolean = false,
    isDeleteMode: Boolean = false,
    onNoteUpdate: (Note) -> Unit = {},
    onDeleteNote: (Int) -> Unit = {},
    onRestore: () -> Unit = {},
    onSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredNotes = filterNotes(notes, searchText)
    if (filteredNotes.isEmpty()) {
        Placeholder(
            placeholderIcon = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    EmptyLeftButton(
                        onNoteClicked = onNoteClicked,
                        shape = shape
                    )
                    EmptyRestoreButton(
                        settingsViewModel = settingsViewModel,
                        shape = shape,
                        onRestore = onRestore
                    )
                    EmptyRightButton(
                        onRightAction = onSearch,
                        shape = shape
                    )
                }
            },
            placeholderText = getEmptyText(searchText)
        )
    } else {
        NotesGrid(
            settingsViewModel = settingsViewModel,
            containerColor = containerColor,
            onNoteClicked = onNoteClicked,
            notes = filteredNotes,
            shape = shape,
            onNoteUpdate = onNoteUpdate,
            selectedNotes = selectedNotes,
            viewMode = viewMode,
            isDeleteClicked = isDeleteMode,
            animationFinished = onDeleteNote
        )
    }
}

private fun filterNotes(notes: List<Note>, searchText: String?): List<Note> {
    return searchText?.takeIf { it.isNotBlank() }?.let { query ->
        notes.filter { note ->
            note.name.contains(query, ignoreCase = true) || note.description.contains(query, ignoreCase = true)
        }
    } ?: notes
}

@Composable
private fun getEmptyText(searchText: String?): String {
    return stringResource(R.string.startup)
}

@Composable
fun EmptyRestoreButton(
    settingsViewModel: SettingsViewModel,
    shape: RoundedCornerShape,
    onRestore: () -> Unit
) {
    val context = LocalContext.current
    var showPasswordPrompt by remember { mutableStateOf(false) }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) settingsViewModel.onImportBackup(uri, context)
        }
    )

    Button(
        onClick = {
            if (settingsViewModel.settings.value.encryptBackup) {
                showPasswordPrompt = true
            } else {
                settingsViewModel.password = null
                importBackupLauncher.launch(arrayOf("application/zip"))
                onRestore()
            }
        },
        shape = shape,
        modifier = Modifier
            .size(100.dp)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudDownload,
            contentDescription = stringResource(R.string.restore),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(48.dp)
        )
    }

    if (showPasswordPrompt) {
        PasswordPrompt(
            context = context,
            text = stringResource(R.string.restore),
            settingsViewModel = settingsViewModel,
            onExit = { password ->
                if (password != null) settingsViewModel.password = password.text
                showPasswordPrompt = false
                importBackupLauncher.launch(arrayOf("application/zip"))
                onRestore()
            },
            onBackup = {
                importBackupLauncher.launch(arrayOf("application/zip"))
            }
        )
    }
}

@Composable
fun EmptyLeftButton(
    onNoteClicked: (Int) -> Unit,
    shape: RoundedCornerShape
) {
    Button(
        onClick = { onNoteClicked(0) },
        shape = shape,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.AddCircle,
            contentDescription = stringResource(R.string.add_note),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun EmptyRightButton(
    onRightAction: () -> Unit,
    shape: RoundedCornerShape
) {
    Button(
        onClick = onRightAction,
        shape = shape,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Share,
            contentDescription = stringResource(R.string.share),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
    }
}