package com.kin.easynotes.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun NotesButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = text,
                modifier = Modifier.padding(end = 9.dp)
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = text,
            )
        }
    }
}