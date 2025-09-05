package com.ezpnix.writeon.presentation.screens.home.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.components.markdown.MarkdownText
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    settingsViewModel: SettingsViewModel,
    containerColor: Color,
    note: Note,
    isBorderEnabled: Boolean,
    shape: RoundedCornerShape,
    onShortClick: () -> Unit,
    onLongClick: () -> Unit,
    onNoteUpdate: (Note) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .padding(4.dp)
            .clip(shape)
            .combinedClickable(
                onClick = { onShortClick() },
                onLongClick = { onLongClick() }
            )
            .then(
                if (isBorderEnabled) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = shape
                ) else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (containerColor != Color.Black) containerColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp, 16.dp)
                .fillMaxWidth()
        ) {
            if (note.name.isNotBlank()) {
                MarkdownText(
                    isPreview = false,
                    isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                    markdown = note.name,
                    containerColor = containerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .heightIn(max = dimensionResource(R.dimen.max_name_height)),
                    weight = FontWeight.Bold,
                    spacing = 0.dp,
                    onContentChange = { onNoteUpdate(note.copy(name = it)) },
                    fontSize = 20.sp,
                    radius = settingsViewModel.settings.value.cornerRadius,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (note.name.isNotBlank() && note.description.isNotBlank() && !settingsViewModel.settings.value.showOnlyTitle) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (note.description.isNotBlank() && !settingsViewModel.settings.value.showOnlyTitle) {
                MarkdownText(
                    isPreview = false,
                    markdown = note.description,
                    isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                    spacing = 0.dp,
                    containerColor = containerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .heightIn(max = dimensionResource(R.dimen.max_description_height)),
                    onContentChange = { onNoteUpdate(note.copy(description = it)) },
                    fontSize = 14.sp,
                    radius = settingsViewModel.settings.value.cornerRadius
                )
            }
            if (note.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = "Tagged Note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = note.tags.joinToString(", ", limit = 2, truncated = "..."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = 100.dp)
                    )
                }
            }
        }
    }
}