package com.ezpnix.writeon.presentation.screens.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ezpnix.writeon.R
import com.ezpnix.writeon.presentation.components.BrowserButton
import com.ezpnix.writeon.presentation.components.CalButton
import com.ezpnix.writeon.presentation.components.CalculatorButton
import com.ezpnix.writeon.presentation.components.CopyButton
import com.ezpnix.writeon.presentation.components.MoreButton
import com.ezpnix.writeon.presentation.components.NavigationIcon
import com.ezpnix.writeon.presentation.components.NotesScaffold
import com.ezpnix.writeon.presentation.components.RedoButton
import com.ezpnix.writeon.presentation.components.SaveButton
import com.ezpnix.writeon.presentation.components.UndoButton
import com.ezpnix.writeon.presentation.components.markdown.MarkdownText
import com.ezpnix.writeon.presentation.screens.edit.components.CustomIconButton
import com.ezpnix.writeon.presentation.screens.edit.components.CustomTextField
import com.ezpnix.writeon.presentation.screens.edit.components.TextFormattingToolbar
import com.ezpnix.writeon.presentation.screens.edit.model.EditViewModel
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager
import com.ezpnix.writeon.presentation.screens.settings.widgets.ActionType
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingsBox
import com.ezpnix.writeon.presentation.screens.settings.widgets.copyToClipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import com.ezpnix.writeon.presentation.components.TranslateButton
import com.ezpnix.writeon.presentation.components.TxtButton
import com.ezpnix.writeon.presentation.components.openTranslateApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditNoteView(
    id: Int,
    settingsViewModel: SettingsViewModel,
    encrypted: Boolean = false,
    onClickBack: () -> Unit
) {
    val viewModel: EditViewModel = hiltViewModel<EditViewModel>()
    viewModel.updateIsEncrypted(encrypted)
    viewModel.setupNoteData(id)
    ObserveLifecycleEvents(viewModel)

    val pagerState = rememberPagerState(initialPage = if (id == 0) 0 else 1, pageCount = { 2 })

    val coroutineScope = rememberCoroutineScope()

    NotesScaffold(
        topBar = { if (!settingsViewModel.settings.value.minimalisticMode) TopBar(pagerState,onClickBack, viewModel) },
        content = { PagerContent(pagerState, coroutineScope, viewModel, settingsViewModel, onClickBack) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopBarActions(pagerState: PagerState, onClickBack: () -> Unit, viewModel: EditViewModel) {
    val context = LocalContext.current
    val isPinned = viewModel.isPinned.value

    when (pagerState.currentPage) {

        0 -> {
            Row {
                if (viewModel.isDescriptionInFocus.value) {
                    RedoButton { viewModel.redo() }
                }
                SaveButton { onClickBack() }
            }
        }
        1 -> {
            Row {
                MoreButton {
                    viewModel.toggleEditMenuVisibility(true)
                }
                DropdownMenu(
                    expanded = viewModel.isEditMenuVisible.value,
                    onDismissRequest = { viewModel.toggleEditMenuVisibility(false) }
                ) {
                    if (viewModel.noteId.value != 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pin)) },
                            leadingIcon = { Icon(if (viewModel.isPinned.value) Icons.Rounded.PushPin else Icons.Outlined.PushPin, contentDescription = "Pin")},
                            onClick = {
                                val message = if (isPinned) {
                                    "Unpinned Note"
                                } else {
                                    "Pinned Note"
                                }
                                viewModel.toggleNotePin(!isPinned)
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.share)) },
                            leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = "Share") },
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, viewModel.noteDescription.value.text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.information_dropdown)) },
                            leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = "Information")},
                            onClick = {
                                viewModel.toggleEditMenuVisibility(false)
                                viewModel.toggleNoteInfoVisibility(true)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerContent(pagerState: PagerState, coroutineScope: CoroutineScope, viewModel: EditViewModel,settingsViewModel: SettingsViewModel, onClickBack: () -> Unit) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.imePadding(),
    ) { page ->
        when (page) {
            0 -> EditScreen(viewModel, coroutineScope, settingsViewModel, pagerState, onClickBack)
            1 -> PreviewScreen(viewModel, settingsViewModel, pagerState, onClickBack)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopBar(pagerState: PagerState, onClickBack: () -> Unit, viewModel: EditViewModel) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        title = { CustomTextField(
            value = viewModel.noteName.value,
            onValueChange = { viewModel.updateNoteName(it) },
            placeholder = stringResource(R.string.no_title),
            enabled = pagerState.currentPage == 0,
            modifier = Modifier.fillMaxWidth(),
        ) },
        navigationIcon = {
            Row {
                NavigationIcon(onClickBack)
                if (pagerState.currentPage == 0 && viewModel.isDescriptionInFocus.value) {
                    UndoButton { viewModel.undo() }
                }
            }
     },
        actions = { TopBarActions(pagerState,  onClickBack, viewModel) }
    )
}

@Composable
fun ObserveLifecycleEvents(viewModel: EditViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveNote(viewModel.noteId.value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(viewModel: EditViewModel, settingsViewModel: SettingsViewModel) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = { viewModel.toggleNoteInfoVisibility(false) }
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        Column(
            modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 20.dp)
        ) {
            SettingsBox(
                isBig = false,
                title = stringResource(R.string.created_time),
                icon = Icons.Rounded.Numbers,
                actionType = ActionType.TEXT,
                radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius),
                customText = sdf.format(viewModel.noteCreatedTime.value).toString()
            )
            SettingsBox(
                isBig = false,
                title = stringResource(R.string.words),
                icon = Icons.Rounded.Numbers,
                radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                actionType = ActionType.TEXT,
                customText = if (viewModel.noteDescription.value.text != "") viewModel.noteDescription.value.text.split("\\s+".toRegex()).size.toString() else "0"
            )
            SettingsBox(
                isBig = false,
                title = stringResource(R.string.characters),
                icon = Icons.Rounded.Numbers,
                actionType = ActionType.TEXT,
                radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                customText = viewModel.noteDescription.value.text.length.toString()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalisticMode(
    alignment : Alignment.Vertical = Alignment.CenterVertically,
    viewModel: EditViewModel,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    isEnabled: Boolean, pagerState: PagerState,
    isExtremeAmoled: Boolean,
    showOnlyDescription: Boolean = false,
    onClickBack: () -> Unit, content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .then(if (showOnlyDescription) Modifier.padding(top = 8.dp) else Modifier)
    ) {
        if (!showOnlyDescription) {
            if (isEnabled) NavigationIcon(onClickBack)
            if (isEnabled && viewModel.isDescriptionInFocus.value) UndoButton { viewModel.undo() }
            content()
            if (isEnabled) TopBarActions(pagerState,  onClickBack, viewModel)
            if (isEnabled) ModeButton(pagerState, coroutineScope, isMinimalistic = true, isExtremeAmoled = isExtremeAmoled) } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEnabled) NavigationIcon(onClickBack)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isEnabled) ModeButton(pagerState, coroutineScope, isMinimalistic = true, isExtremeAmoled = isExtremeAmoled)
                    if (isEnabled) TopBarActions(pagerState,  onClickBack, viewModel)
                }
                content()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    coroutineScope: CoroutineScope,
    settingsViewModel: SettingsViewModel,
    pagerState: PagerState,
    onClickBack: () -> Unit
) {
    val context = LocalContext.current
    val showButtons = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Main content (input field and toolbar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (viewModel.isDescriptionInFocus.value && settingsViewModel.settings.value.isMarkdownEnabled) {
                TextFormattingToolbar(viewModel)
            }
            MarkdownBox(
                isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
                shape = shapeManager(
                    radius = settingsViewModel.settings.value.cornerRadius,
                    isFirst = true
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { viewModel.toggleIsDescriptionInFocus(it.isFocused) }
                    .padding(bottom = 8.dp),
                content = {
                    CustomTextField(
                        value = viewModel.noteDescription.value,
                        onValueChange = { viewModel.updateNoteDescription(it) },
                        modifier = Modifier.fillMaxSize(),
                        placeholder = stringResource(R.string.description),
                        textStyle = TextStyle(fontSize = settingsViewModel.settings.value.fontSize.sp)
                    )
                }
            )
        }

        // Floating buttons layer (fully detached from content)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Positioned at the bottom-center
                .padding(16.dp), // Adjust spacing from screen edges
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Toggle button to show/hide other buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingActionButton(
                    onClick = { showButtons.value = !showButtons.value },
                    containerColor = MaterialTheme.colorScheme.surface, // Background color
                    contentColor = MaterialTheme.colorScheme.primary // Icon color
                ) {
                    Icon(
                        imageVector = if (showButtons.value) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Toggle Buttons"
                    )
                }
            }

            // Conditionally display the buttons based on showButtons state
            if (showButtons.value) {
                // First row with CalButton, ModeButton, TranslateButton
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape) // Background color for row
                        .padding(8.dp) // Padding around the buttons
                ) {
                    TranslateButton(viewModel, onClick = {
                        copyToClipboard(context, viewModel.noteDescription.value.text)
                        openTranslateApp(context, viewModel.noteDescription.value.text)
                    })
                    CopyButton(viewModel, onClick = {
                        copyToClipboard(context, viewModel.noteDescription.value.text)})
                    ModeButton(pagerState, coroutineScope)
                    CalButton()
                    CalculatorButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(viewModel: EditViewModel, settingsViewModel: SettingsViewModel, pagerState: PagerState, onClickBack: () -> Unit) {
    if (viewModel.isNoteInfoVisible.value) BottomModal(viewModel, settingsViewModel)

    val focusManager = LocalFocusManager.current
    focusManager.clearFocus()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        MarkdownBox(
            isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
            shape = shapeManager(
                radius = settingsViewModel.settings.value.cornerRadius,
                isFirst = true
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            content = {
                MarkdownText(
                    radius = settingsViewModel.settings.value.cornerRadius,
                    markdown = viewModel.noteDescription.value.text,
                    isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    fontSize = settingsViewModel.settings.value.fontSize.sp,
                    onContentChange = { viewModel.updateNoteDescription(TextFieldValue(text = it)) }
                )
            }
        )
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Scroll Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val firstVisibleItemIndex = listState.firstVisibleItemIndex
                        listState.animateScrollToItem(maxOf(0, firstVisibleItemIndex - 1)) // Move left
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Scroll Left")
            }

            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                    .padding(8.dp)
                    .weight(1f) // Makes sure it expands properly
            ) {
                item { ModeButton(pagerState, coroutineScope) }
                item { CalButton() }
                item { CalculatorButton() }
                item {
                    TranslateButton(viewModel, onClick = {
                        copyToClipboard(context, viewModel.noteDescription.value.text)
                        openTranslateApp(context, viewModel.noteDescription.value.text)
                    })
                }
                item { CopyButton(viewModel, onClick = {
                    copyToClipboard(context, viewModel.noteDescription.value.text)
                }) }
                item { BrowserButton() }
                item { TxtButton(currentText = viewModel.noteDescription.value.text) }
            }

            // Right Scroll Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val firstVisibleItemIndex = listState.firstVisibleItemIndex
                        listState.animateScrollToItem(minOf(listState.layoutInfo.totalItemsCount - 1, firstVisibleItemIndex + 1)) // Move right
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Scroll Right")
            }
        }
    }
}

@Composable
fun MarkdownBox(
    isExtremeAmoled: Boolean,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        shape = shape,
        modifier = modifier
            .clip(shape)
            .heightIn(max = 128.dp, min = 42.dp)
            .then(
                if (isExtremeAmoled) {
                    Modifier.border(
                        1.5.dp,
                        shape = shape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!isExtremeAmoled) 6.dp else 0.dp),

        ) {
        content()
    }
    Spacer(modifier = Modifier.height(3.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModeButton(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    isMinimalistic: Boolean = false,
    isExtremeAmoled: Boolean = false,
) {
    Row {
        if (!isMinimalistic) {
            RenderButton(
                pagerState,
                coroutineScope,
                0,
                Icons.Rounded.Edit,
                false,
                isExtremeAmoled
            )
            RenderButton(
                pagerState,
                coroutineScope,
                1,
                Icons.Rounded.Description,
                false,
                isExtremeAmoled
            )
        } else {
            val currentPage = pagerState.currentPage
            val icon = if (currentPage == 1) Icons.Rounded.Edit else Icons.Rounded.RemoveRedEye
            RenderButton(pagerState, coroutineScope, if (currentPage == 1) 0 else 1, icon, true, isExtremeAmoled)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RenderButton(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    pageIndex: Int,
    icon: ImageVector,
    isMinimalistic: Boolean,
    isExtremeAmoled: Boolean
) {
    CustomIconButton(
        shape = if (isMinimalistic) RoundedCornerShape(100) else if (pageIndex == 0) RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp) else RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp),
        onClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pageIndex)
            }
        },
        icon = icon,
        elevation = when {
            isExtremeAmoled || isMinimalistic -> 0.dp
            pagerState.currentPage != pageIndex -> 6.dp
            else -> 12.dp
        }
    )
}
