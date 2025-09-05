package com.ezpnix.writeon.presentation.screens.home.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.domain.usecase.NoteUseCase
import com.ezpnix.writeon.presentation.components.DecryptionResult
import com.ezpnix.writeon.presentation.components.EncryptionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val encryptionHelper: EncryptionHelper,
    val noteUseCase: NoteUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    var selectedNotes = mutableStateListOf<Note>()
    private val _isDeleteMode = mutableStateOf(false)
    val isDeleteMode: State<Boolean> get() = _isDeleteMode
    private val _isPasswordPromptVisible = mutableStateOf(false)
    val isPasswordPromptVisible: State<Boolean> get() = _isPasswordPromptVisible
    private val _isVaultMode = mutableStateOf(false)
    val isVaultMode: State<Boolean> get() = _isVaultMode
    private val _isTrashMode = mutableStateOf(false)
    val isTrashMode: State<Boolean> get() = _isTrashMode
    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> get() = _searchQuery
    var showTagDialog = mutableStateOf(false)
    var showRemoveTagDialog = mutableStateOf(false)
    var newTagName = mutableStateOf("")
    var selectedTagsToRemove = mutableStateListOf<String>()
    var currentFilter = mutableStateOf("all")

    init {
        Log.d("HomeViewModel", "Initializing HomeViewModel, observing notes")
        noteUseCase.observe()
    }

    fun toggleIsDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
    }

    fun toggleIsVaultMode(enabled: Boolean) {
        _isVaultMode.value = enabled
        if (!enabled) {
            noteUseCase.decryptionResult = DecryptionResult.LOADING
        }
        noteUseCase.observe()
    }

    fun toggleIsPasswordPromptVisible(enabled: Boolean) {
        _isPasswordPromptVisible.value = enabled
    }

    fun toggleTrashMode(enabled: Boolean) {
        _isTrashMode.value = enabled
    }

    fun changeSearchQuery(newValue: String) {
        _searchQuery.value = newValue
    }

    fun setFilter(filter: String) {
        currentFilter.value = filter
    }

    fun pinOrUnpinNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNotes = selectedNotes.map { note ->
                note.copy(pinned = selectedNotes.any { !it.pinned })
            }
            Log.d("HomeViewModel", "Pinning/unpinning notes: ${updatedNotes.map { it.id to it.pinned }}")
            noteUseCase.batchUpdateNotes(updatedNotes)
            launch(Dispatchers.Main) {
                selectedNotes.clear()
                noteUseCase.observe()
            }
        }
    }

    fun moveSelectedNotesToTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNotes = selectedNotes.map { note ->
                note.copy(trashed = true)
            }
            Log.d("HomeViewModel", "Moving notes to trash: ${updatedNotes.map { it.id to it.trashed }}")
            noteUseCase.batchUpdateNotes(updatedNotes)
            launch(Dispatchers.Main) {
                selectedNotes.clear()
                noteUseCase.observe()
            }
        }
    }

    fun addTagToSelectedNotes() {
        val trimmedTag = newTagName.value.trim().lowercase()
        if (trimmedTag.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val updatedNotes = selectedNotes.map { note ->
                    if (trimmedTag !in note.tags.map { it.lowercase() }) {
                        Log.d("HomeViewModel", "Adding tag '$trimmedTag' to note ${note.id}")
                        note.copy(tags = note.tags + trimmedTag)
                    } else {
                        note
                    }
                }.filter { it.tags != selectedNotes.find { n -> n.id == it.id }?.tags }
                if (updatedNotes.isNotEmpty()) {
                    noteUseCase.batchUpdateNotes(updatedNotes)
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Tag '$trimmedTag' added to ${updatedNotes.size} note(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedNotes.clear()
                        newTagName.value = ""
                        noteUseCase.observe()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Tag '$trimmedTag' already exists on all selected notes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context,
                "Tag name cannot be empty",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun removeTagsFromSelectedNotes() {
        if (selectedTagsToRemove.isEmpty()) {
            Toast.makeText(
                context,
                "No tags selected to remove",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNotes = selectedNotes.map { note ->
                val updatedTags = note.tags.filterNot { it.lowercase() in selectedTagsToRemove.map { it.lowercase() } }
                if (updatedTags != note.tags) {
                    Log.d("HomeViewModel", "Removing tags ${selectedTagsToRemove} from note ${note.id}: ${note.tags} -> $updatedTags")
                    note.copy(tags = updatedTags)
                } else {
                    note
                }
            }.filter { it.tags != selectedNotes.find { n -> n.id == it.id }?.tags }
            if (updatedNotes.isNotEmpty()) {
                noteUseCase.batchUpdateNotes(updatedNotes)
                val currentNotes = runBlocking { noteUseCase.getAllNotesFlow().firstOrNull() ?: emptyList() }
                val processedNotes = currentNotes.mapNotNull { note ->
                    if (note.encrypted) {
                        val (decryptedName, nameResult) = encryptionHelper.decrypt(note.name)
                        val (decryptedDescription, descriptionResult) = encryptionHelper.decrypt(note.description)
                        if (nameResult == DecryptionResult.SUCCESS && descriptionResult == DecryptionResult.SUCCESS) {
                            note.copy(name = decryptedName ?: "", description = decryptedDescription ?: "")
                        } else {
                            null
                        }
                    } else {
                        note
                    }
                }
                val newTags = currentNotes.flatMap { it.tags }.map { it.trim().lowercase() }.distinct().sorted()
                noteUseCase.updateNotes(processedNotes)
                noteUseCase.updateTags(newTags)
                Log.d("HomeViewModel", "Fallback refresh: notes=${processedNotes.map { it.id to it.tags }}, tags=$newTags")
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Removed ${selectedTagsToRemove.size} tag(s) from ${updatedNotes.size} note(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedNotes.clear()
                    selectedTagsToRemove.clear()
                    noteUseCase.observe()
                }
            } else {
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "No tags were removed",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedNotes.clear()
                    selectedTagsToRemove.clear()
                    noteUseCase.observe()
                }
            }
        }
    }

    fun deleteTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteUseCase.deleteTagFromNotes(tag)
            launch(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    if (noteUseCase.tags.contains(tag.lowercase())) "Tag '$tag' still in use on some notes" else "Tag '$tag' deleted",
                    Toast.LENGTH_SHORT
                ).show()
                if (currentFilter.value.lowercase() == tag.lowercase()) {
                    setFilter("all")
                }
                noteUseCase.observe()
            }
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteUseCase.restoreFromTrash(note)
            launch(Dispatchers.Main) {
                noteUseCase.observe()
            }
        }
    }

    fun permanentlyDeleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteUseCase.permanentlyDeleteNoteById(note.id)
            launch(Dispatchers.Main) {
                noteUseCase.observe()
            }
        }
    }

    fun restoreSelectedNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNotes = selectedNotes.map { note ->
                note.copy(trashed = false)
            }
            Log.d("HomeViewModel", "Restoring notes: ${updatedNotes.map { it.id to it.trashed }}")
            noteUseCase.batchUpdateNotes(updatedNotes)
            launch(Dispatchers.Main) {
                selectedNotes.clear()
                noteUseCase.observe()
            }
        }
    }

    fun permanentlyDeleteSelectedNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedNotes.forEach { note ->
                noteUseCase.permanentlyDeleteNoteById(note.id)
            }
            launch(Dispatchers.Main) {
                selectedNotes.clear()
                noteUseCase.observe()
            }
        }
    }

    fun getAllNotes(): List<Note> {
        val filteredNotes = if (_isTrashMode.value) {
            runBlocking { noteUseCase.getTrashedNotesFlow().firstOrNull() ?: emptyList() }
        } else {
            noteUseCase.notes.filter {
                it.encrypted == isVaultMode.value && !it.trashed
            }
        }
        when (noteUseCase.decryptionResult) {
            DecryptionResult.LOADING -> { /* Do nothing */ }
            DecryptionResult.EMPTY -> {
                if (!encryptionHelper.isPasswordEmpty()) toggleIsVaultMode(true)
            }
            DecryptionResult.BAD_PASSWORD,
            DecryptionResult.BLANK_DATA,
            DecryptionResult.INVALID_DATA -> {
                toggleIsVaultMode(false)
                Toast.makeText(
                    context,
                    context.getString(R.string.invalid_password),
                    Toast.LENGTH_SHORT
                ).show()
                encryptionHelper.removePassword()
            }
            else -> toggleIsVaultMode(true)
        }
        return filteredNotes
    }
}