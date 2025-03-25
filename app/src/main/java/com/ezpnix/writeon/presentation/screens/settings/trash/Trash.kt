package com.ezpnix.writeon.presentation.screens.settings.trash

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(navController: NavController, settings: SettingsViewModel) {
    val context = LocalContext.current
    var showInfoPopup by remember { mutableStateOf(false) }
    val iconColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Trash")
                        IconButton(onClick = { showInfoPopup = !showInfoPopup }) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = "Info", tint = iconColor)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Rounded.Restore, contentDescription = "Restore", tint = iconColor)
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Rounded.SelectAll, contentDescription = "Select All", tint = iconColor)
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = "Delete Forever", tint = iconColor)
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Trash Icon",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Coming Soon!",
                        style = TextStyle()
                    )
                }
                if (showInfoPopup) {
                    Popup(alignment = Alignment.TopCenter) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "About: Feature coming soon from developer.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
