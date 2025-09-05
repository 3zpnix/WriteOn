package com.ezpnix.writeon.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LabelOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.domain.model.Note
import com.ezpnix.writeon.presentation.components.*
import com.ezpnix.writeon.presentation.navigation.NavRoutes
import com.ezpnix.writeon.presentation.screens.home.viewmodel.HomeViewModel
import com.ezpnix.writeon.presentation.screens.home.widgets.NoteFilter
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
@Suppress("DEPRECATION")
fun HomeView(
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (Int, Boolean) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val username by settingsModel.dynamicPlaceholder.collectAsState()
    var showDimBackground by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val displayedNotes = viewModel.noteUseCase.notes
        .filter {
            it.encrypted == viewModel.isVaultMode.value &&
                    (if (viewModel.currentFilter.value == "all") true
                    else if (viewModel.currentFilter.value == "pinned") it.pinned
                    else viewModel.currentFilter.value in it.tags)
        }
        .sortedWith(sorter(settingsModel.settings.value.sortDescending))
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                DrawerSectionHeader("Homepage")
                DrawerItem("All Notes") {
                    viewModel.currentFilter.value = "all"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Pinned Notes") {
                    viewModel.currentFilter.value = "pinned"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Trash") {
                    navController.navigate(NavRoutes.Trash.route)
                    scope.launch { drawerState.close() }
                }
                Spacer(modifier = Modifier.height(24.dp))
                DrawerSectionHeader("Tags")
                if (viewModel.noteUseCase.tags.isEmpty()) {
                    Text(
                        text = "No tags yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    viewModel.noteUseCase.tags.forEach { tag ->
                        DeletableDrawerItem(
                            text = tag,
                            onClick = {
                                viewModel.currentFilter.value = tag
                                scope.launch { drawerState.close() }
                            },
                            onDeleteClick = {
                                viewModel.deleteTag(tag)
                            }
                        )
                    }
                }
            }
        },
        content = {
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
                                allNotes = displayedNotes,
                                settingsModel = settingsModel,
                                onPinClick = { viewModel.pinOrUnpinNotes() },
                                onMoveToTrashClick = { viewModel.moveSelectedNotesToTrash() },
                                onSelectAllClick = { selectAllNotes(viewModel, displayedNotes) },
                                onCloseClick = { viewModel.selectedNotes.clear() },
                                onTagClick = { viewModel.showTagDialog.value = true },
                                onRemoveTagClick = { viewModel.showRemoveTagDialog.value = true }
                            )
                        }
                        AnimatedVisibility(
                            visible = viewModel.selectedNotes.isEmpty(),
                            enter = defaultScreenEnterAnimation(),
                            exit = defaultScreenExitAnimation()
                        ) {
                            HomeViewTopBarWithSearch(
                                username = username,
                                query = viewModel.searchQuery.value,
                                onQueryChange = { viewModel.changeSearchQuery(it) },
                                onClearClick = { viewModel.changeSearchQuery("") },
                                onSettingsClick = onSettingsClicked,
                                navController = navController,
                                onCloseSearch = { viewModel.changeSearchQuery("") },
                                onMenuClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open()
                                        else drawerState.close()
                                    }
                                }
                            )
                        }
                    },
                    content = {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NoteFilter(
                                modifier = Modifier.weight(1f),
                                navController = navController,
                                settingsViewModel = settingsModel,
                                containerColor = getContainerColor(settingsModel),
                                shape = shapeManager(
                                    radius = settingsModel.settings.value.cornerRadius / 2,
                                    isBoth = true
                                ),
                                onNoteClicked = { onNoteClicked(it, viewModel.isVaultMode.value) },
                                notes = displayedNotes,
                                selectedNotes = viewModel.selectedNotes,
                                viewMode = settingsModel.settings.value.viewMode,
                                searchText = viewModel.searchQuery.value.ifBlank { null },
                                isDeleteMode = viewModel.isDeleteMode.value,
                                onNoteUpdate = { note ->
                                    scope.launch(Dispatchers.IO) {
                                        viewModel.noteUseCase.addNote(
                                            note
                                        )
                                    }
                                },
                                onDeleteNote = {
                                    val note = displayedNotes.find { note -> note.id == it }
                                    note?.let { n -> viewModel.noteUseCase.moveToTrash(n) }
                                }
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
                    onDimToggle = { showDimBackground = it },
                    settingsModel = settingsModel
                )
            }
            if (viewModel.showTagDialog.value) {
                AlertDialog(
                    onDismissRequest = { viewModel.showTagDialog.value = false },
                    title = { Text("Add Tag") },
                    text = {
                        TextField(
                            value = viewModel.newTagName.value,
                            onValueChange = { viewModel.newTagName.value = it },
                            label = { Text("Tag name") }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.addTagToSelectedNotes()
                            viewModel.showTagDialog.value = false
                            viewModel.newTagName.value = ""
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { viewModel.showTagDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            if (viewModel.showRemoveTagDialog.value) {
                val uniqueTags = viewModel.selectedNotes.flatMap { it.tags }.distinct()
                if (uniqueTags.isNotEmpty()) {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.showRemoveTagDialog.value = false
                            viewModel.selectedTagsToRemove.clear()
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (viewModel.selectedTagsToRemove.isNotEmpty()) {
                                    viewModel.removeTagsFromSelectedNotes()
                                    viewModel.showRemoveTagDialog.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please select at least one tag to remove",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Text("Remove")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                viewModel.showRemoveTagDialog.value = false
                                viewModel.selectedTagsToRemove.clear()
                            }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Remove Tags") },
                        text = {
                            Column {
                                uniqueTags.forEach { tag ->
                                    var isChecked by remember(tag) {
                                        mutableStateOf(
                                            viewModel.selectedTagsToRemove.contains(
                                                tag
                                            )
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isChecked = !isChecked
                                                viewModel.selectedTagsToRemove.apply {
                                                    if (isChecked && tag !in this) add(tag)
                                                    else if (!isChecked && tag in this) remove(tag)
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                isChecked = checked
                                                viewModel.selectedTagsToRemove.apply {
                                                    if (checked && tag !in this) add(tag)
                                                    else if (!checked && tag in this) remove(tag)
                                                }
                                            }
                                        )
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                } else {
                    viewModel.showRemoveTagDialog.value = false
                    Toast.makeText(context, "No tags to remove", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@Composable
fun DeletableDrawerItem(
    text: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            Log.d("HomeView", "Delete button clicked for tag: $text")
            onDeleteClick()
        }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Tag",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeViewTopBarWithSearch(
    username: String = "User",
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSettingsClick: () -> Unit,
    navController: NavController,
    onCloseSearch: () -> Unit,
    onMenuClick: () -> Unit
) {
    var searchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val animatedHeight by animateDpAsState(
        targetValue = if (searchActive) 70.dp else 56.dp,
        animationSpec = tween(250),
        label = "SearchHeightAnim"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!searchActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onMenuClick() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Drawer"
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WriteOn",
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
                IconButton(onClick = { onSettingsClick() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .focusRequester(focusRequester),
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onQueryChange,
            active = searchActive,
            onActiveChange = { searchActive = it },
            placeholder = { Text(stringResource(R.string.search), maxLines = 1) },
            leadingIcon = {
                if (searchActive) {
                    IconButton(onClick = {
                        searchActive = false
                        onCloseSearch()
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                } else {
                    MainButton { navController.navigate(NavRoutes.ColorStyles.route) }
                }
            },
            trailingIcon = {
                Row {
                    if (query.isNotBlank()) {
                        CloseButton(
                            contentDescription = "Clear",
                            onCloseClicked = onClearClick
                        )
                    }
                    IconButton(onClick = {
                        searchActive = true
                        focusRequester.requestFocus()
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Activate Search")
                    }
                }
            }
        ) {}
    }
}

@Composable
fun getContainerColor(settingsModel: SettingsViewModel): Color {
    return if (settingsModel.settings.value.extremeAmoledMode) Color.Black
    else MaterialTheme.colorScheme.surfaceContainerHigh
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FloatingBottomButtons(
    navController: NavController,
    onNoteClicked: (Int) -> Unit,
    onDimToggle: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    settingsModel: SettingsViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCalculator by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "ScaleAnimation"
    )
    val calendarState = rememberUseCaseState()
    val selectedDates = remember { mutableStateOf<List<LocalDate>>(listOf()) }
    val disabledDates = listOf(LocalDate.now())
    val allFabItems = listOf(
        Triple(Icons.Default.Search, "Browser") {
            val url = "https://www.startpage.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                context.startActivity(intent)
                Toast.makeText(context, "Opening Default Browser...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "No browser found!", Toast.LENGTH_SHORT).show()
            }
        },
        Triple(Icons.Default.Dataset, "Flashcard") { navController.navigate(NavRoutes.Flashback.route) },
        Triple(Icons.Default.AddComment, "Save TXT") { showDialog = true },
        Triple(Icons.Default.NoteAlt, "Scratchpad") { navController.navigate(NavRoutes.Scratchpad.route) },
        Triple(Icons.Default.Calculate, "Calculator") { showCalculator = true },
        Triple(Icons.Default.CalendarMonth, "Calendar") { calendarState.show() },
        Triple(Icons.Default.Delete, "Recycle Bin") { navController.navigate(NavRoutes.Trash.route) },
        Triple(Icons.Default.Edit, "New Note") { onNoteClicked(0) }
    )
    val visibleItems by settingsModel.visibleFabItems.collectAsState()
    val fabItems = allFabItems.filter { visibleItems.contains(it.second) } +
            Triple(Icons.Default.Settings, "Modify") { showEditDialog = true }
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(textState.toByteArray())
                    }
                    Toast.makeText(context, "Text saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        selection = CalendarSelection.Dates { selectedDates.value = it }
    )
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
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Quick Export") },
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
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Customize Quick Actions") },
            text = {
                Column {
                    allFabItems.forEach { (_, label, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsModel.updateVisibleFabItems(
                                        if (label in visibleItems) visibleItems - label else visibleItems + label
                                    )
                                }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = label in visibleItems,
                                onCheckedChange = {
                                    settingsModel.updateVisibleFabItems(
                                        if (it) visibleItems + label else visibleItems - label
                                    )
                                }
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val fabSize = 56.dp
        val spacing = 12.dp
        val totalWidth = fabSize * 2 + spacing + 16.dp
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(totalWidth)
        ) {
            fabItems.forEach { (icon, label, action) ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    SmallFAB(
                        icon = icon,
                        description = label,
                        onClick = {
                            onExpandedChange(false)
                            onDimToggle(false)
                            action()
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { onNoteClicked(0) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New Note",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    FloatingActionButton(
                        onClick = {},
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
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
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
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
    onMoveToTrashClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onCloseClick: () -> Unit,
    onTagClick: () -> Unit,
    onRemoveTagClick: () -> Unit
) {
    var deleteAlert by remember { mutableStateOf(false) }
    if (deleteAlert) {
        AlertDialog(
            onDismissRequest = { deleteAlert = false },
            title = { Text(text = stringResource(id = R.string.alert_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteAlert = false
                        onMoveToTrashClick()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.yes),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteAlert = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
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
                DeleteButton(onClick = { deleteAlert = true })
                Spacer(modifier = Modifier.width(5.dp))
                PinButton(isPinned = selectedNotes.all { it.pinned }, onClick = onPinClick)
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = onTagClick) {
                    Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Add Tag")
                }
                Spacer(modifier = Modifier.width(5.dp))
                if (selectedNotes.any { it.tags.isNotEmpty() }) {
                    IconButton(onClick = onRemoveTagClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LabelOff,
                            contentDescription = "Remove Tag"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))
                SelectAllButton(enabled = selectedNotes.size != allNotes.size, onClick = onSelectAllClick)
            }
        }
    )
}

private fun selectAllNotes(viewModel: HomeViewModel, allNotes: List<Note>) {
    val updatedSelection = allNotes.filterNot { it in viewModel.selectedNotes }
    viewModel.selectedNotes.addAll(updatedSelection)
}

fun sorter(descending: Boolean): Comparator<Note> {
    return if (descending) {
        compareByDescending { it.createdAt }
    } else {
        compareBy { it.createdAt }
    }
}