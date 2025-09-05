package com.ezpnix.writeon.domain.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ezpnix.writeon.data.local.database.Converters
@Entity(tableName = "notes-table")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "note-name")
    val name: String,
    @ColumnInfo(name = "note-description")
    val description: String,
    @ColumnInfo(name = "pinned")
    val pinned: Boolean = false,
    @ColumnInfo(name = "encrypted")
    val encrypted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    var trashed: Boolean = false,
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList()
)