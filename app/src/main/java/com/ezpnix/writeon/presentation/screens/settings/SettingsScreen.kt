package com.ezpnix.writeon.presentation.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.ConnectionConst
import com.ezpnix.writeon.presentation.components.NavigationIcon
import com.ezpnix.writeon.presentation.components.NotesScaffold
import com.ezpnix.writeon.presentation.components.TitleText
import com.ezpnix.writeon.presentation.navigation.NavRoutes
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.ContributorsClicked
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager
import com.ezpnix.writeon.presentation.screens.settings.widgets.ActionType
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingCategory
import com.ezpnix.writeon.presentation.screens.settings.widgets.SettingsBox

@Composable
fun SettingsScaffold(
    settingsViewModel: SettingsViewModel,
    title: String,
    onBackNavClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    NotesScaffold(
        topBar = {
            key(settingsViewModel.settings.value) {
                TopBar(title, onBackNavClicked)
            }
        },
        content = {
            Box(Modifier.padding(16.dp, 8.dp, 16.dp)) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onBackNavClicked: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        title = {
            TitleText(titleText = title)
        },
        navigationIcon = { NavigationIcon { onBackNavClicked() } }
    )
}

@Composable
fun MainSettings(settingsViewModel: SettingsViewModel,navController: NavController) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.screen_settings),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingCategory(
                    smallSetting = true,
                    title = stringResource(id = R.string.support),
                    icon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    isLast = true,
                    composableAction = { onExit -> BottomModal(navController = navController, settingsViewModel = settingsViewModel) { onExit() }}
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.color_styles),
                    subTitle = stringResource(R.string.description_color_styles),
                    icon = Icons.Rounded.Palette,
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    action = { navController.navigate(NavRoutes.ColorStyles.route) })
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.privacy),
                    subTitle = stringResource(id = R.string.backup_restore),
                    icon = ImageVector.vectorResource(id = R.drawable.incognito_fill),
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = false),
                    action = { navController.navigate(NavRoutes.Privacy.route) }
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.tools),
                    subTitle = stringResource(R.string.description_tools),
                    icon = Icons.Rounded.TextFields,
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    isLast = true,
                    action = { navController.navigate(NavRoutes.Tools.route) })
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.about),
                    subTitle = stringResource(R.string.description_about),
                    icon = Icons.Rounded.Info,
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    action = { navController.navigate(NavRoutes.About.route) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(navController: NavController,settingsViewModel: SettingsViewModel, onExit: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = { onExit() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 20.dp)
        ) {
            SettingsBox(
                title = stringResource(id = R.string.app_list),
                icon = Icons.Rounded.VolunteerActivism,
                isCentered = true,
                actionType = ActionType.CUSTOM,
                radius = shapeManager(isBoth = true, radius = settingsViewModel.settings.value.cornerRadius),
                customAction = { onExit -> ContributorsClicked(list = ConnectionConst.APP_LIST, settingsViewModel = settingsViewModel) { onExit() } }
            )
            Spacer(modifier = Modifier.height(18.dp))
            SettingsBox(
                isBig = false,
                title = "YouTube",
                icon = Icons.Rounded.Verified,
                isCentered = true,
                actionType = ActionType.CUSTOM,
                radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius),
                customAction = { uriHandler.openUri(ConnectionConst.YOUTUBE) }
            )
            SettingsBox(
                title = "Github",
                isBig = false,
                isCentered = true,
                icon = Icons.Rounded.Android,
                radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                actionType = ActionType.CUSTOM,
                customAction = { uriHandler.openUri(ConnectionConst.GITHUB) }
            )
            SettingsBox(
                title = "KoFi",
                isBig = false,
                icon = Icons.Rounded.Coffee,
                isCentered = true,
                actionType = ActionType.CUSTOM,
                radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                customAction = { LaunchedEffect(true) { uriHandler.openUri(ConnectionConst.KOFI) } }
            )
        }
    }
}
