package com.ezpnix.writeon.presentation.screens.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import com.ezpnix.writeon.data.local.database.NoteDatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object TxtBackupHelper {

    data class ExportNote(val title: String, val content: String)

    suspend fun writeNotesToZipStream(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext
        ZipOutputStream(outputStream).use { zipOut ->
            getAllNotes(context).forEachIndexed { index, note ->
                val baseName = note.title.ifBlank { "Untitled" }
                val filename = sanitizeFileName("${index + 1}_$baseName") + ".txt"
                zipOut.putNextEntry(ZipEntry(filename))
                zipOut.write(note.content.toByteArray())
                zipOut.closeEntry()
            }
        }
    }

    suspend fun restoreNotesFromZipStream(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext
        val dbProvider = NoteDatabaseProvider(context.applicationContext as Application)
        val dao = dbProvider.noteDao()

        java.util.zip.ZipInputStream(inputStream).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".txt")) {
                    val content = zipIn.reader().readText()

                    val fileName = entry.name.removeSuffix(".txt")
                    val title = fileName.substringAfter("_").ifBlank { "Untitled" }

                    dao.addNote(
                        com.ezpnix.writeon.domain.model.Note(
                            id = 0,
                            name = title,
                            description = content
                        )
                    )
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }

    private suspend fun getAllNotes(context: Context): List<ExportNote> {
        val dbProvider = NoteDatabaseProvider(context.applicationContext as Application)
        val dao = dbProvider.noteDao()
        return dao.getAllNotesSync().map {
            ExportNote(it.name, it.description)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
}