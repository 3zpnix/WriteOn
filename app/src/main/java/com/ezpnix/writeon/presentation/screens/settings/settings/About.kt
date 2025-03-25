package com.ezpnix.writeon.presentation.screens.settings.settings

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.ConnectionConst
import com.ezpnix.writeon.presentation.components.TitleText
import com.ezpnix.writeon.presentation.screens.settings.SettingsScaffold
import com.ezpnix.writeon.presentation.screens.settings.TitleText
import com.ezpnix.writeon.presentation.screens.settings.formatStorage
import com.ezpnix.writeon.presentation.screens.settings.loadState
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.saveState
import com.ezpnix.writeon.presentation.screens.settings.widgets.ActionType
import com.ezpnix.writeon.presentation.screens.settings.widgets.ListDialog
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingsBox

@Composable
fun AboutScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val uriHandler = LocalUriHandler.current
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.about),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    title = stringResource(id = R.string.latest_release),
                    icon = Icons.Rounded.Verified,
                    actionType = ActionType.LINK,
                    radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius),
                    linkClicked = { uriHandler.openUri("https://f-droid.org/en/packages/com.ezpnix.writeon/") }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.source_code),
                    icon = Icons.Rounded.Download,
                    actionType = ActionType.LINK,
                    radius = shapeManager(isLast = true, radius = settingsViewModel.settings.value.cornerRadius),
                    linkClicked = { uriHandler.openUri("https://github.com/3zpnix/WriteOn/") }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.build_type),
                    description = settingsViewModel.build,
                    icon = Icons.Rounded.Build,
                    actionType = ActionType.TEXT,
                    radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius)
                )
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.version),
                    description = settingsViewModel.version,
                    icon = Icons.Rounded.Info,
                    actionType = ActionType.TEXT,
                    radius = shapeManager(isLast = true, radius = settingsViewModel.settings.value.cornerRadius),
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    title = stringResource(id = R.string.email),
                    description = stringResource(id = R.string.email_description),
                    icon = Icons.Rounded.Email,
                    actionType = ActionType.LINK,
                    linkClicked = { uriHandler.openUri("https://github.com/3zpnix/WriteOn/issues/new") },
                    radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius),
                )
            }
            item {
                SettingsBox(
                    isBig = true,
                    title = stringResource(id = R.string.homepage),
                    icon = Icons.Rounded.Home,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(isLast = true, radius = settingsViewModel.settings.value.cornerRadius),
                    customAction = { navController.navigateUp() }
                )
            }
            item {
                AnnouncementsSection(settingsViewModel, context = LocalContext.current, navController)
            }
        }
    }
}


@Composable
fun ContributorsClicked(
    list: List<Pair<String, String>>,
    settingsViewModel: SettingsViewModel,
    onExit: () -> Unit
) {
    ListDialog(
        text = stringResource(R.string.app_list),
        list = list,
        settingsViewModel = settingsViewModel,
        onExit = onExit,
        extractDisplayData = { it }
    ) { isFirstItem, isLastItem, displayData ->
        SettingsBox(
            title = displayData.first,
            description = displayData.second,
            radius = shapeManager(isFirst = isFirstItem, isLast = isLastItem, radius = settingsViewModel.settings.value.cornerRadius),
            actionType = ActionType.TEXT,
            customText = "✅"
        )
    }
}

@Composable
fun AnnouncementsSection(settingsViewModel: SettingsViewModel, context: Context, navController: NavController) {
    val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val savedState = remember { mutableStateOf(loadState(preferences)) }

    val isExpanded = savedState.value

    Column(modifier = Modifier.fillMaxWidth()) {
        val isLightMode = !MaterialTheme.colorScheme.primaryContainer.luminance().isNaN() &&
                MaterialTheme.colorScheme.primaryContainer.luminance() > 0.5

        val alphaValue = if (isLightMode) 1.0f else 0.2f

        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = alphaValue),
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TitleText(
                        titleText = stringResource(id = R.string.announcements),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        val newState = !isExpanded
                        savedState.value = newState
                        saveState(preferences, newState)
                    }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .height(150.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TitleText(titleText = "- Updated home user interface")
                        TitleText(titleText = "- Searchbar placeholder feature")
                        TitleText(titleText = "- Fixed custom size dimensions")
                        TitleText(titleText = "- Directly calculate within the app")
                        TitleText(titleText = "- Ability to change font size")
                        TitleText(titleText = "- Added more featured buttons")
                        TitleText(titleText = "- Calendar date issue fixed")
                        TitleText(titleText = "- Renamed some strings")
                        TitleText(titleText = "- Squished some bugs")


                    }
                }
            }
        }
    }
}


