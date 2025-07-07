package com.ezpnix.writeon.presentation.screens.home.viewmodel

import android.content.Context
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val encryptionHelper: EncryptionHelper,
    val noteUseCase: NoteUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var selectedNotes = mutableStateListOf<Note>()

    val notesFlow = noteUseCase.getAllNotesFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshNotes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
        }
    }

    private var _isDeleteMode = mutableStateOf(false)
    val isDeleteMode: State<Boolean> = _isDeleteMode

    private var _isPasswordPromptVisible = mutableStateOf(false)
    val isPasswordPromptVisible: State<Boolean> = _isPasswordPromptVisible

    private var _isVaultMode = mutableStateOf(false)
    val isVaultMode: State<Boolean> = _isVaultMode

    private var _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        observeNotes()
    }

    private fun observeNotes() {
        noteUseCase.observe()
        viewModelScope.launch {
            noteUseCase.getAllNotesFlow().collectLatest { allNotes ->
                val filteredNotes = allNotes.filter { it.encrypted == _isVaultMode.value }
                handleDecryptionState()
                _notes.value = filteredNotes
            }
        }
    }

    private fun handleDecryptionState() {
        when (noteUseCase.decryptionResult) {
            DecryptionResult.LOADING -> {}
            DecryptionResult.EMPTY -> {
                if (!encryptionHelper.isPasswordEmpty()) {
                    toggleIsVaultMode(true)
                }
            }
            DecryptionResult.BAD_PASSWORD,
            DecryptionResult.BLANK_DATA,
            DecryptionResult.INVALID_DATA -> {
                toggleIsVaultMode(false)
                Toast.makeText(context, context.getString(R.string.invalid_password), Toast.LENGTH_SHORT).show()
                encryptionHelper.removePassword()
            }
            else -> {
                toggleIsVaultMode(true)
            }
        }
    }

    fun toggleIsDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
    }

    fun toggleIsVaultMode(enabled: Boolean) {
        _isVaultMode.value = enabled
        if (!enabled) {
            noteUseCase.decryptionResult = DecryptionResult.LOADING
        }
        observeNotes()
    }

    fun toggleIsPasswordPromptVisible(enabled: Boolean) {
        _isPasswordPromptVisible.value = enabled
    }

    fun changeSearchQuery(newValue: String) {
        _searchQuery.value = newValue
    }

    fun pinOrUnpinNotes() {
        if (selectedNotes.all { it.pinned }) {
            selectedNotes.forEach { note ->
                val updatedNote = note.copy(pinned = false)
                noteUseCase.pinNote(updatedNote)
            }
        } else {
            selectedNotes.forEach { note ->
                val updatedNote = note.copy(pinned = true)
                noteUseCase.pinNote(updatedNote)
            }
        }
        selectedNotes.clear()
    }
}
