package com.ezpnix.oracleswift.data.repository

import com.ezpnix.oracleswift.data.local.database.NoteDatabaseProvider
import com.ezpnix.oracleswift.domain.model.Note
import com.ezpnix.oracleswift.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val provider: NoteDatabaseProvider
) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> {
        return provider.noteDao().getAllNotes()
    }

    override suspend fun addNote(note: Note) {
        provider.noteDao().addNote(note)
    }

    override suspend fun updateNote(note: Note) {
        provider.noteDao().updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        provider.noteDao().deleteNote(note)
    }

    override fun getNoteById(id: Int): Flow<Note> {
        return provider.noteDao().getNoteById(id)
    }

    override fun getLastNoteId(): Long? {
        return provider.noteDao().getLastNoteId()
    }
}