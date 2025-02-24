package com.ezpnix.writeon.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.ezpnix.writeon.R

@Composable
fun CloseButton(
    contentDescription: String = "Close",
    onCloseClicked:  () -> Unit
) {
    IconButton(onClick = onCloseClicked) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MoreButton(onClick: () -> Unit) {
    IconButton(onClick = { onClick() }) {
        Icon(Icons.Rounded.BubbleChart, contentDescription = "Info")
    }
}

@Composable
fun SaveButton(onSaveClicked: () -> Unit) {
    IconButton(onClick = onSaveClicked) {
        Icon(
            imageVector = Icons.Rounded.Done,
            contentDescription = "Done",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun UndoButton(onUndoClicked: () -> Unit) {
    IconButton(onClick = onUndoClicked) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Undo,
            contentDescription = "Undo",
            tint = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun RedoButton(onRedoClicked: () -> Unit) {
    IconButton(onClick = onRedoClicked) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Redo,
            contentDescription = "Redo",
            tint = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun NavigationIcon(onBackNavClicked: () -> Unit) {
    IconButton(onClick = onBackNavClicked) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SettingsButton(onSettingsClicked: () -> Unit) {
    IconButton(onClick = onSettingsClicked) {
        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun PrivacyButton(onSettingsClicked: () -> Unit) {
    IconButton(onClick = onSettingsClicked) {
        Icon(
            ImageVector.vectorResource(id = R.drawable.incognito_fill),
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun TitleText(titleText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titleText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PinButton(isPinned: Boolean, onClick: () -> Unit) {
    IconButton(onClick = { onClick() }) {
        Icon(if (isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin, contentDescription = "Pin")
    }
}

@Composable
fun DeleteButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() }
    ) {
        Icon(
            imageVector =  Icons.Rounded.Delete,
            contentDescription = "Delete",
        )
    }
}

@Composable
fun SelectAllButton(enabled: Boolean, onClick: () -> Unit) {
    if (enabled) {
        IconButton(
            onClick = { onClick() }
        ) {
            Icon(
                imageVector =  Icons.Rounded.SelectAll,
                contentDescription = "Select All",
            )
        }
    }
}

@Composable
fun MainButton(onSettingsClicked: () -> Unit) {
    IconButton(onClick = onSettingsClicked) {
        Icon(
            imageVector =  Icons.Rounded.AccountCircle,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}