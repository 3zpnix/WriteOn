package com.ezpnix.writeon.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.components.CalculatorUI
import com.ezpnix.writeon.presentation.components.CloseButton
import com.ezpnix.writeon.presentation.components.DeleteButton
import com.ezpnix.writeon.presentation.components.MainButton
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeView(
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (Int, Boolean) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val notes by viewModel.notesFlow.collectAsState(initial = emptyList())
    val placeholder by settingsModel.dynamicPlaceholder.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshNotes() }
    )
    var showDimBackground by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var fabExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        NotesScaffold(
            topBar = {
                AnimatedVisibility(
                    visible = viewModel.selectedNotes.isNotEmpty(),
                    enter = defaultScreenEnterAnimation(),
                    exit = defaultScreenExitAnimation()
                ) {
                    SelectedNotesTopAppBar(
                        selectedNotes = viewModel.selectedNotes,
                        allNotes = notes,
                        settingsModel = settingsModel,
                        onPinClick = { viewModel.pinOrUnpinNotes() },
                        onDeleteClick = { viewModel.toggleIsDeleteMode(true) },
                        onSelectAllClick = { selectAllNotes(viewModel, notes) },
                        onCloseClick = { viewModel.selectedNotes.clear() }
                    )
                }

                AnimatedVisibility(
                    visible = viewModel.selectedNotes.isEmpty(),
                    enter = defaultScreenEnterAnimation(),
                    exit = defaultScreenExitAnimation()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HomeViewTopBarWithSearch(
                            username = placeholder,
                            query = viewModel.searchQuery.value,
                            onQueryChange = { viewModel.changeSearchQuery(it) },
                            onClearClick = { viewModel.changeSearchQuery("") },
                            onSettingsClick = onSettingsClicked,
                            placeholderText = placeholder,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    NoteFilter(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        settingsViewModel = settingsModel,
                        containerColor = getContainerColor(settingsModel),
                        shape = shapeManager(
                            radius = settingsModel.settings.value.cornerRadius / 2,
                            isBoth = true
                        ),
                        onNoteClicked = { onNoteClicked(it, viewModel.isVaultMode.value) },
                        notes = notes.sortedWith(sorter(settingsModel.settings.value.sortDescending)),
                        selectedNotes = viewModel.selectedNotes,
                        viewMode = settingsModel.settings.value.viewMode,
                        searchText = viewModel.searchQuery.value.ifBlank { null },
                        isDeleteMode = viewModel.isDeleteMode.value,
                        onNoteUpdate = { note ->
                            coroutineScope.launch(Dispatchers.IO) {
                                viewModel.noteUseCase.addNote(note)
                            }
                        },
                        onDeleteNote = {
                            viewModel.toggleIsDeleteMode(false)
                            viewModel.noteUseCase.deleteNoteById(it)
                        }
                    )

                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        )

        if (showDimBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        showDimBackground = false
                        fabExpanded = false
                    }
            )
        }
        FloatingBottomButtons(
            navController = navController,
            onNoteClicked = { onNoteClicked(it, viewModel.isVaultMode.value) },
            expanded = fabExpanded,
            onExpandedChange = { fabExpanded = it },
            onDimToggle = { showDimBackground = it }
        )
    }
}


        @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeViewTopBarWithSearch(
    username: String = "User",
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSettingsClick: () -> Unit,
    placeholderText: String,
    navController: NavController,
    viewModel: HomeViewModel
) {
    var searchActive by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (!searchActive) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.home_app_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    maxLines = 2,
                    softWrap = true
                )
            }
        }

        SearchBar(
            modifier = if (searchActive) Modifier
                .fillMaxWidth()
                .height(70.dp)
            else Modifier
                .widthIn(min = 140.dp, max = 160.dp)
                .height(56.dp),
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onQueryChange,
            active = searchActive,
            onActiveChange = { active -> searchActive = active },
            placeholder = {
                Text(stringResource(id = R.string.search_label), maxLines = 1)
            },
            leadingIcon = {
                if (searchActive) {
                    IconButton(onClick = { searchActive = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_action_description)
                        )
                    }
                } else {
                    MainButton { navController.navigate(NavRoutes.ColorStyles.route) }
                }
            },
                    trailingIcon = {
                Row {
                    if (query.isNotBlank()) {
                        CloseButton(contentDescription = stringResource(id = R.string.clear_action_description), onCloseClicked = onClearClick)
                    }
                    SettingsButton(onSettingsClicked = onSettingsClick)
                }
            }
        ) {}
    }
}


@Composable
fun getContainerColor(settingsModel: SettingsViewModel): Color {
    return if (settingsModel.settings.value.extremeAmoledMode) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class)
@Composable
fun FloatingBottomButtons(
    navController: NavController,
    onNoteClicked: (Int) -> Unit,
    onDimToggle: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showCalculator by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "ScaleAnimation"
    )

    val calendarState = rememberUseCaseState()
    val selectedDates = remember { mutableStateOf<List<LocalDate>>(listOf()) }
    val disabledDates = listOf(LocalDate.now())
    var textState by remember { mutableStateOf("") }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                try {
                    activity.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(textState.toByteArray())
                    }
                    val msg = context.getString(R.string.saved_successfully_message)
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    val msg = context.getString(R.string.error_message)
                    Toast.makeText(activity, "$msg ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

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

    if (showCalculator) {
        AlertDialog(
            onDismissRequest = { showCalculator = false },
            title = { Text(stringResource(id = R.string.open_calculator_menu)) },
            text = { CalculatorUI() },
            confirmButton = {
                Button(onClick = { showCalculator = false }) {
                    Text(stringResource(id = R.string.close_button))
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.quick_note_dialog_title)) },
            text = {
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text(stringResource(id = R.string.quick_note_dialog_enter_text_hint)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    openFileLauncher.launch("rename.txt")
                    showDialog = false
                }) {
                    Text(stringResource(id = R.string.save_txt_button))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.cancel_button))
                }
            }
        )
    }

    val fabItems = listOf(
        Triple(Icons.Default.Search, stringResource(id = R.string.open_browser_menu)) {
            val url = "https://www.startpage.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                context.startActivity(intent)
                val msg = context.getString(R.string.opening_default_browser_label)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val msg = context.getString(R.string.no_browser_found_label)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        },
        Triple(Icons.Default.NoteAlt, stringResource(id = R.string.open_scratchpad_menu)) {
            navController.navigate(NavRoutes.Scratchpad.route)
        },
        Triple(Icons.Default.Dataset, stringResource(id = R.string.open_flashcard_menu)) {
            navController.navigate(NavRoutes.Flashback.route)
        },
        Triple(Icons.Default.AddComment, stringResource(id = R.string.save_new_txt_menu)) {
            showDialog = true
        },
        Triple(Icons.Default.Calculate, stringResource(id = R.string.open_calculator_menu)) {
            showCalculator = true
        },
        Triple(Icons.Default.CalendarMonth, stringResource(id = R.string.open_calendar_menu)) {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
            val msg = context.getString(R.string.today_is, dayOfWeek, currentDate)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            calendarState.show()
        },
        Triple(Icons.Default.Edit, stringResource(id = R.string.new_note_menu)) {
            onNoteClicked(0)
            val msg = context.getString(R.string.note_created_snackbar_label)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp, end = 24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            fabItems.forEach { (icon, label, action) ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    SmallFAB(icon = icon, description = label) {
                        onExpandedChange(false)
                        onDimToggle(false)
                        action()
                    }
                }
            }

            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(60.dp)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> isPressed = true
                            MotionEvent.ACTION_UP -> {
                                isPressed = false
                                onExpandedChange(!expanded)
                                onDimToggle(!expanded)
                            }
                            MotionEvent.ACTION_CANCEL -> isPressed = false
                        }
                        true
                    }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SmallFAB(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 6.dp,
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(24.dp)
            )
        }
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

private fun selectAllNotes(viewModel: HomeViewModel, allNotes: List<Note>) {
    val updatedSelection = viewModel.selectedNotes.toMutableList()
    allNotes.forEach { note ->
        if (!updatedSelection.contains(note)) {
            updatedSelection.add(note)
        }
    }
    viewModel.selectedNotes.clear()
    viewModel.selectedNotes.addAll(updatedSelection)
}

fun sorter(descending: Boolean): Comparator<Note> {
    return if (descending) {
        compareByDescending { it.createdAt }
    } else {
        compareBy { it.createdAt }
    }
}