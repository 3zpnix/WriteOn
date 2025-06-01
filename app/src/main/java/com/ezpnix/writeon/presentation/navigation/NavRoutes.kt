package com.ezpnix.writeon.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.AndroidScreen
import com.ezpnix.writeon.presentation.screens.settings.FlashcardScreen
import com.ezpnix.writeon.presentation.screens.settings.MainSettings
import com.ezpnix.writeon.presentation.screens.settings.ScratchpadScreen
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.AboutScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.ColorStylesScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.LanguageScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.MarkdownScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.PrivacyScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.IssueScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.ToolsScreen
import com.ezpnix.writeon.presentation.screens.settings.settings.GuideScreen
import com.ezpnix.writeon.presentation.screens.settings.trash.TrashScreen

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Edit : NavRoutes("edit/{id}/{encrypted}") {
        fun createRoute(id: Int, encrypted : Boolean) = "edit/$id/$encrypted"
    }
    data object Terms : NavRoutes("terms")
    data object Settings : NavRoutes("settings")
    data object ColorStyles : NavRoutes("settings/color_styles")
    data object Language : NavRoutes("settings/language")
    data object Privacy : NavRoutes("settings/privacy")
    data object Markdown : NavRoutes("settings/markdown")
    data object Tools : NavRoutes("settings/tools")
    data object About : NavRoutes("settings/about")
    data object Scratchpad: NavRoutes("scratchpad")
    data object Issue: NavRoutes("issue")
    data object Guide: NavRoutes("guide")
    data object Trash: NavRoutes("trash")
    data object Flashback: NavRoutes("flashback")
    data object Android: NavRoutes("android")
}

val settingScreens = mapOf<String, @Composable (settingsViewModel: SettingsViewModel, navController : NavController) -> Unit>(
    NavRoutes.Settings.route to { settings, navController -> MainSettings(settings, navController) },
    NavRoutes.ColorStyles.route to { settings, navController -> ColorStylesScreen(navController,settings) },
    NavRoutes.Language.route to { settings, navController -> LanguageScreen(navController,settings) },
    NavRoutes.Privacy.route to { settings, navController -> PrivacyScreen(navController, settings) },
    NavRoutes.Markdown.route to { settings, navController -> MarkdownScreen(navController,settings) },
    NavRoutes.Tools.route to { settings, navController -> ToolsScreen(navController,settings) },
    NavRoutes.About.route to { settings, navController -> AboutScreen(navController,settings) },
    NavRoutes.Scratchpad.route to { settings, navController -> ScratchpadScreen(navController,settings) },
    NavRoutes.Issue.route to { settings, navController -> IssueScreen(navController,settings) },
    NavRoutes.Guide.route to { settings, navController -> GuideScreen(navController, settings) },
    NavRoutes.Trash.route to { settings, navController -> TrashScreen(navController, settings) },
    NavRoutes.Flashback.route to { settings, navController -> FlashcardScreen(navController, settings) },
    NavRoutes.Android.route to { settings, navController -> AndroidScreen(navController, settings) },
)
