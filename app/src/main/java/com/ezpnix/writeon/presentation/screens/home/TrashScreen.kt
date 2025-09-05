package com.ezpnix.writeon.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ezpnix.writeon.domain.model.Note
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.ui.res.stringResource
import com.ezpnix.writeon.R
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.home.viewmodel.HomeViewModel
import com.ezpnix.writeon.presentation.screens.home.widgets.NotesGrid
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val trashedNotes by viewModel.noteUseCase.getTrashedNotesFlow()
        .collectAsState(initial = emptyList())

    val selectedNotes = remember { mutableStateListOf<Note>() }
    var longPressedNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.trash),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            selectedNotes.forEach { viewModel.restoreNote(it) }
                            selectedNotes.clear()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedNotes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.RestoreFromTrash, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.restore))
                    }

                    OutlinedButton(
                        onClick = {
                            selectedNotes.forEach { viewModel.permanentlyDeleteNote(it) }
                            selectedNotes.clear()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = selectedNotes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.delete_forever))
                    }
                }
            }

            if (trashedNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.trash_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                NotesGrid(
                    settingsViewModel = settingsViewModel,
                    containerColor = getContainerColor(settingsViewModel),
                    onNoteClicked = { noteId ->
                        trashedNotes.find { it.id == noteId }?.let { note ->
                            if (selectedNotes.contains(note)) selectedNotes.remove(note)
                            else selectedNotes.add(note)
                        }
                    },
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius / 2,
                        isBoth = true
                    ),
                    notes = trashedNotes,
                    onNoteUpdate = {},
                    selectedNotes = selectedNotes,
                    viewMode = true,
                    isDeleteClicked = false,
                    animationFinished = {}
                )
            }
        }
    }

    if (longPressedNote != null) {
        ModalBottomSheet(onDismissRequest = { longPressedNote = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = longPressedNote?.name ?: "",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        longPressedNote?.let { viewModel.restoreNote(it) }
                        longPressedNote = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.restore))
                }

                OutlinedButton(
                    onClick = {
                        longPressedNote?.let { viewModel.permanentlyDeleteNote(it) }
                        longPressedNote = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.delete_forever))
                }
            }
        }
    }
}
