package com.ezpnix.oracleswift.domain.repository

import com.ezpnix.oracleswift.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    fun getNoteById(id: Int): Flow<Note>
    fun getLastNoteId(): Long?
}