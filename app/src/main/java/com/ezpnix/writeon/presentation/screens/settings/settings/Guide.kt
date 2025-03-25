package com.ezpnix.writeon.presentation.screens.settings.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current

    val faqList = listOf(
        "What is Write On?" to "Write On is a simple app for taking notes—nothing special. You can add images and many more.",
        "How do I delete a note?" to "Long-press a note on your home screen to see the available options.",
        "Can I recover deleted notes?" to "Currently, there is no recovery feature, so be careful when deleting notes. But, this feature is coming real soon so stay tune.",
        "How often are updates released?" to "The developer usually releases updates every 1–2 months, depending on the schedule.",
        "How do I report an issue or bug?" to "You can visit the official GitHub page, found in the About section, and create an issue there.",
        "Do you accept feature requests?" to "Of course! Any feedback or suggestions for improvement are welcome.",
        "Automatic backup files stored?" to "They are currently located in the Android/data folder, which, unfortunately, is inaccessible on some Android 10+ devices.",
        "Is this app fully open-source?" to "Yes! There’s nothing shady—no crypto, no forced ads—just a clean, open-source app.",
        "Who developed this app?" to "3zpnix, an individual who wanted a note-taking app with enough features for daily use."
    )

    var expandedStates by remember { mutableStateOf(List(faqList.size) { false }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guide & FAQ", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(faqList) { index, (question, answer) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = question,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = {
                                expandedStates = expandedStates.toMutableList().apply {
                                    this[index] = !this[index]
                                }
                            }) {
                                Icon(
                                    imageVector = if (expandedStates[index]) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Expand/Collapse",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (expandedStates[index]) {
                            Text(
                                text = answer,
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}