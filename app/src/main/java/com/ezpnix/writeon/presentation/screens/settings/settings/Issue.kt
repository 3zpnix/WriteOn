package com.ezpnix.writeon.presentation.screens.settings.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select an Item") })
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(items.size) { index ->
                GridItem(itemName = items[index], onClick = {
                })
            }
        }
    }
}

@Composable
fun GridItem(itemName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(16.dp)
                .height(100.dp)
        ) {
            Text(
                text = itemName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
