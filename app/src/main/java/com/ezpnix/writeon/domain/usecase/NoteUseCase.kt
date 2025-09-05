package com.ezpnix.writeon.domain.usecase

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import com.ezpnix.writeon.data.repository.NoteRepositoryImpl
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.components.DecryptionResult
import com.ezpnix.writeon.presentation.components.EncryptionHelper
import com.ezpnix.writeon.widget.NotesWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepositoryImpl,
    private val coroutineScope: CoroutineScope,
    private val encryptionHelper: EncryptionHelper,
    @ApplicationContext private val context: Context
) {
    var notes: List<Note> by mutableStateOf(emptyList())
        private set
    var tags: List<String> by mutableStateOf(emptyList())
        private set
    var decryptionResult: DecryptionResult by mutableStateOf(DecryptionResult.LOADING)
    private var observeKeysJob: Job? = null
    private var observeTagsJob: Job? = null

    fun observe() {
        observeNotes()
        observeTags()
    }

    private fun observeNotes() {
        observeKeysJob?.cancel()
        observeKeysJob = coroutineScope.launch {
            getAllNotes().collectLatest { notes ->
                val hasUnencryptedNotes = notes.any { !it.encrypted }
                if (!hasUnencryptedNotes) this@NoteUseCase.decryptionResult = DecryptionResult.EMPTY
                val processedNotes = notes.mapNotNull { note ->
                    if (note.encrypted) {
                        val (decryptedNote, status) = decryptNote(note)
                        this@NoteUseCase.decryptionResult = status
                        if (status == DecryptionResult.SUCCESS) decryptedNote else null
                    } else {
                        note
                    }
                }
                Log.d("NoteUseCase", "Updated notes: ${processedNotes.map { it.id to it.tags }}")
                this@NoteUseCase.notes = processedNotes
                NotesWidget().updateAll(context)
            }
        }
    }

    private fun observeTags() {
        observeTagsJob?.cancel()
        observeTagsJob = coroutineScope.launch {
            noteRepository.getAllNotes().collectLatest { notes ->
                val newTags = notes.flatMap { it.tags }
                    .map { it.trim().lowercase() }
                    .distinct()
                    .sorted()
                    .filter { it.isNotBlank() }
                Log.d("NoteUseCase", "Updated tags from Flow: $newTags")
                tags = newTags
            }
        }
    }

    private fun encryptNote(note: Note): Note {
        return if (note.encrypted) {
            note.copy(
                name = encryptionHelper.encrypt(note.name),
                description = encryptionHelper.encrypt(note.description),
                encrypted = true
            )
        } else {
            note
        }
    }

    private fun decryptNote(note: Note): Pair<Note, DecryptionResult> {
        val (decryptedName, nameResult) = encryptionHelper.decrypt(note.name)
        val (decryptedDescription, descriptionResult) = encryptionHelper.decrypt(note.description)
        return if (note.encrypted) {
            Pair(
                note.copy(
                    name = decryptedName ?: "",
                    description = decryptedDescription ?: "",
                ), descriptionResult
            )
        } else {
            Pair(note, DecryptionResult.SUCCESS)
        }
    }

    fun updateNotes(newNotes: List<Note>) {
        Log.d("NoteUseCase", "Manually updating notes: ${newNotes.map { it.id to it.tags }}")
        notes = newNotes.mapNotNull { note ->
            if (note.encrypted) {
                val (decryptedNote, status) = decryptNote(note)
                if (status == DecryptionResult.SUCCESS) decryptedNote else null
            } else {
                note
            }
        }
    }

    fun updateTags(newTags: List<String>) {
        Log.d("NoteUseCase", "Manually updating tags: $newTags")
        tags = newTags
    }

    fun getAllNotesFlow(): Flow<List<Note>> = getAllNotes()

    private fun getAllNotes(): Flow<List<Note>> {
        return noteRepository.getAllNotes()
    }

    fun getNotesByTagFlow(tag: String): Flow<List<Note>> {
        return noteRepository.getNotesByTag(tag.trim().lowercase())
    }

    suspend fun addNote(note: Note) {
        val noteToSave = if (note.encrypted) {
            encryptNote(note.copy(tags = note.tags.map { it.trim().lowercase() }.distinct()))
        } else {
            note.copy(tags = note.tags.map { it.trim().lowercase() }.distinct())
        }
        Log.d("NoteUseCase", "Saving note ${noteToSave.id} with tags ${noteToSave.tags}")
        if (noteToSave.id == 0) {
            noteRepository.addNote(noteToSave)
        } else {
            noteRepository.updateNote(noteToSave)
            val updatedNote = noteRepository.getNoteById(noteToSave.id).first()
            Log.d("NoteUseCase", "Note ${noteToSave.id} after update has tags: ${updatedNote.tags}")
        }
    }

    suspend fun batchUpdateNotes(notes: List<Note>) {
        val notesToSave = notes.map { note ->
            if (note.encrypted) encryptNote(note.copy(tags = note.tags.map { it.trim().lowercase() }.distinct())) else note
        }
        Log.d("NoteUseCase", "Batch updating notes: ${notesToSave.map { it.id to it.tags }}")
        Log.d("NoteUseCase", "Tags JSON: ${com.ezpnix.writeon.data.local.database.Converters().fromStringList(notesToSave.firstOrNull()?.tags ?: emptyList())}")
        noteRepository.batchUpdateNotes(notesToSave)
    }

    suspend fun addTagToNote(noteId: Int, tag: String) {
        val trimmedTag = tag.trim().lowercase()
        if (trimmedTag.isNotBlank()) {
            val note = noteRepository.getNoteById(noteId).first()
            if (trimmedTag !in note.tags.map { it.lowercase() }) {
                val updatedNote = note.copy(tags = note.tags + trimmedTag)
                noteRepository.updateNote(updatedNote)
            }
        }
    }

    suspend fun deleteTagFromNotes(tag: String) {
        val trimmedTag = tag.trim().lowercase()
        val allNotes = noteRepository.getAllNotes().first()
        val updatedNotes = allNotes.map { note ->
            val hasTag = trimmedTag in note.tags.map { it.lowercase() }
            if (hasTag) {
                val newTags = note.tags.filterNot { it.lowercase() == trimmedTag }
                Log.d("NoteUseCase", "Removing tag '$trimmedTag' from note ${note.id}: ${note.tags} -> $newTags")
                note.copy(tags = newTags)
            } else {
                note
            }
        }.filter { note -> note.tags != allNotes.find { it.id == note.id }?.tags }

        if (updatedNotes.isNotEmpty()) {
            Log.d("NoteUseCase", "Batch updating ${updatedNotes.size} notes for tag deletion")
            noteRepository.batchUpdateNotes(updatedNotes)
            val freshNotes = noteRepository.getAllNotes().first()
            val computedTags = freshNotes.flatMap { it.tags }
                .map { it.trim().lowercase() }
                .distinct()
                .sorted()
                .filter { it.isNotBlank() }
            updateTags(computedTags)
            Log.d("NoteUseCase", "Tags after deletion: $computedTags")
        } else {
            Log.d("NoteUseCase", "No notes updated for tag '$trimmedTag' (already removed or no matches)")
            val freshNotes = noteRepository.getAllNotes().first()
            val computedTags = freshNotes.flatMap { it.tags }
                .map { it.trim().lowercase() }
                .distinct()
                .sorted()
                .filter { it.isNotBlank() }
            updateTags(computedTags)
            Log.d("NoteUseCase", "Tags after recomputation: $computedTags")
        }
    }

    fun pinNote(note: Note) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            addNote(note)
        }
    }

    fun deleteNoteById(id: Int) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val noteToDelete = noteRepository.getNoteById(id).first()
            noteRepository.deleteNote(noteToDelete)
        }
    }

    fun getNoteById(id: Int): Flow<Note> {
        return noteRepository.getNoteById(id)
    }

    fun getLastNoteId(onResult: (Long?) -> Unit) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val lastNoteId = noteRepository.getLastNoteId()
            withContext(Dispatchers.Main) {
                onResult(lastNoteId)
            }
        }
    }

    suspend fun updatePinStatus(noteId: Int, pinned: Boolean) {
        val noteToUpdate = noteRepository.getNoteById(noteId).first()
        val updatedNote = noteToUpdate.copy(pinned = pinned)
        noteRepository.updateNote(updatedNote)
    }

    fun moveToTrash(note: Note) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val trashedNote = note.copy(trashed = true)
            noteRepository.updateNote(trashedNote)
        }
    }

    fun restoreFromTrash(note: Note) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val restoredNote = note.copy(trashed = false)
            noteRepository.updateNote(restoredNote)
        }
    }

    fun permanentlyDeleteNoteById(id: Int) {
        coroutineScope.launch(NonCancellable + Dispatchers.IO) {
            val noteToDelete = noteRepository.getNoteById(id).first()
            noteRepository.deleteNote(noteToDelete)
        }
    }

    fun getTrashedNotesFlow(): Flow<List<Note>> = noteRepository.getTrashedNotes()
}