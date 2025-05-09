package com.ezpnix.writeon.presentation.screens.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.components.CloseButton
import com.ezpnix.writeon.presentation.components.DeleteButton
import com.ezpnix.writeon.presentation.components.NotesScaffold
import com.ezpnix.writeon.presentation.components.PinButton
import com.ezpnix.writeon.presentation.components.SelectAllButton
import com.ezpnix.writeon.presentation.components.SettingsButton
import com.ezpnix.writeon.presentation.components.TitleText
import com.ezpnix.writeon.presentation.components.defaultScreenEnterAnimation
import com.ezpnix.writeon.presentation.components.defaultScreenExitAnimation
import com.ezpnix.writeon.presentation.navigation.NavRoutes
import com.ezpnix.writeon.presentation.screens.home.viewmodel.HomeViewModel
import com.ezpnix.writeon.presentation.screens.home.widgets.NoteFilter
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.PasswordPrompt
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager
import androidx.compose.runtime.getValue // 3zpnix
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.ezpnix.writeon.presentation.components.CalculatorUI
import com.ezpnix.writeon.presentation.components.MainButton
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun HomeView (
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (Int, Boolean) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    if (viewModel.isPasswordPromptVisible.value) {
        PasswordPrompt(
            context = context,
            text = stringResource(id = R.string.password_continue),
            settingsViewModel = settingsModel,
            onExit = { password ->
                if (password != null && password.text.isNotBlank()) {
                    viewModel.encryptionHelper.setPassword(password.text)
                    viewModel.noteUseCase.observe()
                }
                viewModel.toggleIsPasswordPromptVisible(false)
            }
        )
    }

    if (settingsModel.databaseUpdate.value) viewModel.noteUseCase.observe()
    val containerColor = getContainerColor(settingsModel)

    NotesScaffold(
        topBar = {
            AnimatedVisibility(
                visible = viewModel.selectedNotes.isNotEmpty(),
                enter = defaultScreenEnterAnimation(),
                exit = defaultScreenExitAnimation()
            ) {
                SelectedNotesTopAppBar(
                    selectedNotes = viewModel.selectedNotes,
                    allNotes = viewModel.getAllNotes(),
                    settingsModel = settingsModel,
                    onPinClick = { viewModel.pinOrUnpinNotes() },
                    onDeleteClick = { viewModel.toggleIsDeleteMode(true) },
                    onSelectAllClick = { selectAllNotes(viewModel, viewModel.getAllNotes()) },
                    onCloseClick = { viewModel.selectedNotes.clear() }
                )
            }
            AnimatedVisibility(
                viewModel.selectedNotes.isEmpty(),
                enter = defaultScreenEnterAnimation(),
                exit = defaultScreenExitAnimation()
            ) {
                val dynamicPlaceholder by settingsModel.dynamicPlaceholder.collectAsState()
                NotesSearchBar(
                    settingsModel = settingsModel,
                    query = viewModel.searchQuery.value,
                    onQueryChange = { viewModel.changeSearchQuery(it) },
                    onSettingsClick = onSettingsClicked,
                    onClearClick = { viewModel.changeSearchQuery("") },
                    viewModel = viewModel,
                    navController = navController,
                    placeholderText = dynamicPlaceholder,
                    onVaultClicked = {
                        if (!viewModel.isVaultMode.value) {
                            viewModel.toggleIsPasswordPromptVisible(true)
                        } else {
                            viewModel.toggleIsVaultMode(false)
                            viewModel.encryptionHelper.removePassword()
                        }
                    }
                )
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    NoteFilter(
                        settingsViewModel = settingsModel,
                        containerColor = containerColor,
                        shape = shapeManager(
                            radius = settingsModel.settings.value.cornerRadius / 2,
                            isBoth = true
                        ),
                        onNoteClicked = { onNoteClicked(it, viewModel.isVaultMode.value) },
                        notes = viewModel.getAllNotes().sortedWith(sorter(settingsModel.settings.value.sortDescending)),
                        selectedNotes = viewModel.selectedNotes,
                        viewMode = settingsModel.settings.value.viewMode,
                        searchText = viewModel.searchQuery.value.ifBlank { null },
                        isDeleteMode = viewModel.isDeleteMode.value,
                        onNoteUpdate = { note -> CoroutineScope(Dispatchers.IO).launch { viewModel.noteUseCase.addNote(note) } },
                        onDeleteNote = {
                            viewModel.toggleIsDeleteMode(false)
                            viewModel.noteUseCase.deleteNoteById(it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    BottomButtons(
                        navController = navController,
                        onNoteClicked = { onNoteClicked(it, viewModel.isVaultMode.value) },
                        modifier = Modifier,
                    )
                }
            }
        }
    )
}

@Composable
fun getContainerColor(settingsModel: SettingsViewModel): Color {
    return if (settingsModel.settings.value.extremeAmoledMode) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomButtons(
    navController: NavController,
    onNoteClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = LocalContext.current

    var textState by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                try {
                    activity.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(textState.toByteArray())
                    }
                    Toast.makeText(activity, "Text saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Quick Note") },
            text = {
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Enter text") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    openFileLauncher.launch("rename.txt")
                    showDialog = false
                }) {
                    Text("Save as TXT")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCalculator) {
        AlertDialog(
            onDismissRequest = { showCalculator = false },
            title = { Text("Calculator") },
            text = { CalculatorUI() },
            confirmButton = {
                Button(onClick = { showCalculator = false }) {
                    Text("Close")
                }
            }
        )
    }

    val calendarState = rememberUseCaseState()
    val selectedDates = remember { mutableStateOf<List<LocalDate>>(listOf()) }
    val disabledDates = listOf(LocalDate.now())

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
            disabledDates = disabledDates
        ),
        selection = CalendarSelection.Dates {
            selectedDates.value = it
        }
    )

    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyRow(
            modifier = Modifier
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
    item {
                IconCircleButton(Icons.Default.AddComment, "Save as TXT") {
                    showDialog = true
                }
            }
            item {
                IconCircleButton(Icons.Default.Edit, "Edit Note") {
                    onNoteClicked(0)
                    Toast.makeText(context, "Note Created!", Toast.LENGTH_SHORT).show()
                }
            }
            item {
                IconCircleButton(Icons.Default.CalendarMonth, "Calendar") {
                    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
                    Toast.makeText(context, "Today is: $dayOfWeek, $currentDate", Toast.LENGTH_SHORT).show()
                    calendarState.show()
                }
            }
            item {
                IconCircleButton(Icons.Default.Calculate, "Calculator") {
                    showCalculator = true
                }
            }
            item {
                IconCircleButton(Icons.Default.NoteAlt, "All Notes") {
                    navController.navigate(NavRoutes.OneNote.route)
                }
            }
            item {
                IconCircleButton(Icons.Default.RestoreFromTrash, "Trash") {
                    navController.navigate(NavRoutes.Trash.route)
                }
            }
            item {
                IconCircleButton(Icons.Default.Search, "Calculator") {
                    val url = "https://www.startpage.com"

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                    context.startActivity(intent)
                    Toast.makeText(context, "Opening Default Browser...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
private fun IconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedNotesTopAppBar(
    selectedNotes: List<Note>,
    allNotes: List<Note>,
    settingsModel: SettingsViewModel,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    var deletealert by remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(visible = deletealert) {
        AlertDialog(onDismissRequest = { deletealert = false }, title = {
            Text(
                text = stringResource(id = R.string.alert_text)
            )
        }, confirmButton = {
            TextButton(onClick = { deletealert=false
                onDeleteClick()
            }) {
                Text(text = stringResource(id = R.string.yes), color = MaterialTheme.colorScheme.error )
            }
        },
            dismissButton = {
                TextButton(onClick = { deletealert = false }) {
                    Text(text =stringResource(id = R.string.cancel))
                }
            })
    }
    TopAppBar(
        modifier = Modifier.padding(bottom = 36.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (!settingsModel.settings.value.extremeAmoledMode)
                MaterialTheme.colorScheme.surfaceContainerLow
            else Color.Black
        ),
        title = { TitleText(titleText = selectedNotes.size.toString()) },
        navigationIcon = { CloseButton(onCloseClicked = onCloseClick) },
        actions = {
            Row {
                DeleteButton(onClick =  {deletealert = true})
                Spacer(modifier = Modifier.width(5.dp))
                PinButton(isPinned = selectedNotes.all { it.pinned }, onClick = onPinClick)
                Spacer(modifier = Modifier.width(5.dp))
                SelectAllButton(
                    enabled = selectedNotes.size != allNotes.size,
                    onClick = onSelectAllClick
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesSearchBar(
    settingsModel: SettingsViewModel, // AA1
    viewModel: HomeViewModel, // AA2
    query: String,
    onQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onVaultClicked: () -> Unit, // AA3
    onClearClick: () -> Unit,
    navController: NavController,
    placeholderText: String
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp, vertical = 10.dp),
        query = query,
        placeholder = { Text(placeholderText) },
        leadingIcon = {
            Row {
                MainButton { navController.navigate(NavRoutes.Privacy.route) }
            }
        },
        trailingIcon = {
            Row {
                if (query.isNotBlank()) {
                    CloseButton(contentDescription = "Clear", onCloseClicked = onClearClick)
                }
                SettingsButton(onSettingsClicked = onSettingsClick)
            }
        },
        onQueryChange = onQueryChange,
        onSearch = onQueryChange,
        onActiveChange = {},
        active = false,
    ) {
        // AA4
    }
}

private fun selectAllNotes(viewModel: HomeViewModel, allNotes: List<Note>) {
    allNotes.forEach {
        if (!viewModel.selectedNotes.contains(it)) {
            viewModel.selectedNotes.add(it)
        }
    }
}

fun sorter(descending: Boolean): Comparator<Note> {
    return if (descending) {
        compareByDescending { it.createdAt }
    } else {
        compareBy { it.createdAt }
    }
}