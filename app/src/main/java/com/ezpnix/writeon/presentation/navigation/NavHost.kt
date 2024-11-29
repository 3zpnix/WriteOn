package com.ezpnix.writeon.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.ezpnix.writeon.presentation.screens.edit.EditNoteView
import com.ezpnix.writeon.presentation.screens.home.HomeView
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.terms.TermsScreen

@Composable
fun AppNavHost(settingsModel: SettingsViewModel,navController: NavHostController = rememberNavController(), noteId: Int) {
    val activity = (LocalContext.current as? Activity)

    NavHost(navController, startDestination = if (!settingsModel.settings.value.termsOfService) NavRoutes.Terms.route else if (noteId == -1) NavRoutes.Home.route else NavRoutes.Edit.route) {
        animatedComposable(NavRoutes.Home.route) {
            HomeView(
                onSettingsClicked = { navController.navigate(NavRoutes.Settings.route) },
                onNoteClicked = { id, encrypted -> navController.navigate(NavRoutes.Edit.createRoute(id, encrypted)) },
                settingsModel = settingsModel,
                navController = navController
            )
        }

        animatedComposable(NavRoutes.Terms.route) {
            TermsScreen(
                settingsModel
            )
        }

        animatedComposable(NavRoutes.Edit.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            val encrypted = backStackEntry.arguments?.getString("encrypted").toBoolean()
            EditNoteView(
                settingsViewModel = settingsModel,
                id = if (noteId == -1) id else noteId,
                encrypted = encrypted
            ) {
                if (noteId == -1) {
                    navController.navigateUp()
                } else {
                    activity?.finish()
                }
            }
        }

        settingScreens.forEach { (route, screen) ->
            if (route == NavRoutes.Settings.route) {
                slideInComposable(route) {
                    screen(settingsModel,navController)
                }
            } else {
                animatedComposable(route) {
                    screen(settingsModel,navController)
                }
            }
        }
    }
}
