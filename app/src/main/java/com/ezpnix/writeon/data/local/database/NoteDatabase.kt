package com.ezpnix.writeon.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ezpnix.writeon.core.constant.DatabaseConst
import com.ezpnix.writeon.data.local.dao.NoteDao
import com.ezpnix.writeon.domain.model.Note

@Database(
    entities = [Note::class],
    version = DatabaseConst.NOTES_DATABASE_VERSION,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}