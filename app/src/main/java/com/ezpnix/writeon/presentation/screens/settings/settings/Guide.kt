package com.ezpnix.writeon.presentation.screens.settings.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
        "How do I delete a note?" to "Long-press a note on your home screen to see the available options. You may also discover other hidden features as well such as the swipe to edit/preview note.",
        "Can I recover deleted notes?" to "Currently, there is no recovery feature, so be careful when deleting notes. But, this feature is coming real soon so stay tune.",
        "How often are updates released?" to "The developer usually releases updates every one to two months, depending on how busy the developer is with real life and stuff.",
        "How do I report an issue or bug?" to "You can visit the official GitHub page or come chat with the Developer on his social media account.",
        "Do you accept feature requests?" to "Of course! Any feedback or suggestions for improvement are welcome. The developer usually replies within a day or two.",
        "Automatic backup files stored?" to "They are currently located in the Android/data folder, which, unfortunately, is inaccessible to some because of the latest android policy",
        "Is this app fully open-source?" to "Yes! You may fork it, modify codes to your liking. There’s nothing shady—no virus, no forced ads—just a clean, open-source app.",
        "Who developed this app?" to "@3zpnix, a foreign university student somewhere in the world who got bored and wanted to try on something."
    )

    var expandedStates by remember { mutableStateOf(List(faqList.size) { false }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guide & FAQ", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedStates = expandedStates.toMutableList().apply {
                                this[index] = !this[index]
                            }
                        },
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
                            Icon(
                                imageVector = if (expandedStates[index]) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "Expand/Collapse",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expandedStates[index]) {
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