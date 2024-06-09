package com.kin.easynotes.presentation.screens.home.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kin.easynotes.Notes
import com.kin.easynotes.domain.usecase.NoteUseCase

enum class SortProperty {
    CREATED_DATE,
    LAST_UPDATED_DATE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

open class HomeViewModel() : ViewModel() {
    private val noteRepository = Notes.dataModule.noteRepository
    val noteUseCase = NoteUseCase(noteRepository, viewModelScope)

    var selectedNotes = mutableStateListOf<Int>()

    private var _isDeleteMode = mutableStateOf(false)
    val isDeleteMode: State<Boolean> = _isDeleteMode

    private var _isSortExpanded = mutableStateOf(false)
    val isSortExpanded: State<Boolean> = _isSortExpanded

    fun toggleIsDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
    }
}
