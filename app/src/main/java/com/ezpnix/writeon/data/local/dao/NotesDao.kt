package com.ezpnix.writeon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ezpnix.writeon.domain.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM `notes-table` WHERE trashed = 0")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM `notes-table` WHERE trashed = 0")
    suspend fun getAllNotesSync(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Update
    suspend fun batchUpdateNotes(notes: List<Note>)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM `notes-table` WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note>

    @Query("SELECT id FROM `notes-table` ORDER BY id DESC LIMIT 1")
    fun getLastNoteId(): Long?

    @Query("SELECT * FROM `notes-table` WHERE trashed = 1")
    fun getTrashedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM `notes-table` WHERE :tag IN (tags) AND trashed = 0")
    fun getNotesByTag(tag: String): Flow<List<Note>>
}