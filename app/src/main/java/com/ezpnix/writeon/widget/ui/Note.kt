package com.ezpnix.writeon.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.domain.usecase.NoteUseCase
import com.ezpnix.writeon.presentation.MainActivity
import com.ezpnix.writeon.presentation.components.markdown.WidgetText

@Composable
fun SelectedNote(note: Note, noteUseCase: NoteUseCase, widgetId: Int) {
    val context = LocalContext.current
    val glanceModifier = GlanceModifier.clickable {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", note.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    Column(
        modifier = glanceModifier
            .background(GlanceTheme.colors.background)
            .fillMaxSize()
            .padding(6.dp)
    ) {
        if(note.name.isNotBlank()) {
            WidgetText(
                modifier = glanceModifier,
                markdown = note.name,
                weight = FontWeight.Bold,
                fontSize = 24.sp,
                color = GlanceTheme.colors.primary,
                onContentChange = {
                    noteUseCase.addNote(note.copy(name = it))
                    noteUseCase.observe()
                }
            )
        }
        if(note.description.isNotBlank()) {
            WidgetText(
                modifier = glanceModifier.fillMaxWidth(),
                markdown = note.description,
                weight = FontWeight.Normal,
                fontSize = 12.sp,
                color = GlanceTheme.colors.primary,
                onContentChange = {
                    noteUseCase.addNote(note.copy(description = it))
                    noteUseCase.observe()
                }
            )
        }
    }
}
