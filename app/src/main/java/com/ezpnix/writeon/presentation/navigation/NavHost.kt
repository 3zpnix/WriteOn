package com.ezpnix.writeon.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.ezpnix.writeon.presentation.screens.edit.EditNoteView
import com.ezpnix.writeon.presentation.screens.home.HomeView
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.terms.TermsScreen

@Composable
fun AppNavHost(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    noteId: Int,
    startDestination: String
) {
    NavHost(navController, startDestination = startDestination) {
        animatedComposable(NavRoutes.Home.route) {
            HomeView(
                onSettingsClicked = { navController.navigate(NavRoutes.Settings.route) },
                onNoteClicked = { id, encrypted -> navController.navigate(NavRoutes.Edit.createRoute(id, encrypted)) },
                settingsModel = settingsViewModel,
                navController = navController
            )
        }

        animatedComposable(NavRoutes.Terms.route) {
            TermsScreen(
                settingsViewModel = settingsViewModel,
                navController = navController
            )
        }

        animatedComposable(NavRoutes.Edit.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            val encrypted = backStackEntry.arguments?.getString("encrypted").toBoolean()
            EditNoteView(
                settingsViewModel = settingsViewModel,
                id = if (noteId == -1) id else noteId,
                encrypted = encrypted
            ) {
                if (noteId == -1) {
                    navController.popBackStack()
                } else {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Edit.route) { inclusive = true }
                    }
                }
            }
        }

        settingScreens.forEach { (route, screen) ->
            if (route == NavRoutes.Settings.route) {
                slideInComposable(route) {
                    screen(settingsViewModel, navController)
                }
            } else {
                animatedComposable(route) {
                    screen(settingsViewModel, navController)
                }
            }
        }
    }
}