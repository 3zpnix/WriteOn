package com.ezpnix.writeon.data.repository
import com.ezpnix.writeon.data.local.database.NoteDatabaseProvider
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class NoteRepositoryImpl @Inject constructor(
    private val provider: NoteDatabaseProvider
) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> {
        return provider.noteDao().getAllNotes()
    }
    override suspend fun getAllNotesSync(): List<Note> {
        return provider.noteDao().getAllNotesSync()
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
    override fun getTrashedNotes(): Flow<List<Note>> {
        return provider.noteDao().getTrashedNotes()
    }
    override fun getNotesByTag(tag: String): Flow<List<Note>> {
        return provider.noteDao().getNotesByTag(tag)
    }
    override suspend fun getAllTags(): List<String> {
        return provider.noteDao().getAllNotesSync().flatMap { it.tags }.distinct()
    }

    override suspend fun batchUpdateNotes(notes: List<Note>) {
        provider.noteDao().batchUpdateNotes(notes)
    }
}
