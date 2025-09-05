package com.ezpnix.writeon.presentation.screens.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ezpnix.writeon.R
import com.ezpnix.writeon.core.constant.ConnectionConst
import com.ezpnix.writeon.presentation.components.AgreeButton
import com.ezpnix.writeon.presentation.components.NotesScaffold
import com.ezpnix.writeon.presentation.components.markdown.MarkdownText
import com.ezpnix.writeon.presentation.navigation.NavRoutes
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager

@Composable
fun TermsScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    NotesScaffold(
        floatingActionButton = {
            AgreeButton(text = stringResource(id = R.string.agree)) {
                settingsViewModel.updateTermsOfService(true)
                navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.Terms.route) { inclusive = true }
                }
            }
        },
        content = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.terms_of_service),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(0.dp, 16.dp, 16.dp, 16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            shapeManager(
                                isBoth = true,
                                radius = settingsViewModel.settings.value.cornerRadius
                            )
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                        )
                        .padding(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MarkdownText(
                            fontSize = 12.sp,
                            radius = settingsViewModel.settings.value.cornerRadius,
                            markdown = getTermsOfService(),
                            isEnabled = true
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun getTermsOfService(): String {
    return buildString {
        append("### ${stringResource(R.string.terms_acceptance_title)}\n")
        append("${stringResource(R.string.terms_acceptance_body)}\n\n")

        append("### ${stringResource(R.string.terms_license_title)}\n")
        append("${stringResource(R.string.terms_license_body)}\n\n")

        append("### ${stringResource(R.string.terms_responsibilities_title)}\n")
        append("${stringResource(R.string.terms_responsibilities_body)}\n\n")

        append("### ${stringResource(R.string.terms_liabilities_title)}\n")
        append("${stringResource(R.string.terms_liabilities_body)}\n\n")

        append("### ${stringResource(R.string.terms_warranties_title)}\n")
        append("${stringResource(R.string.terms_warranties_body)}\n\n")

        append("### ${stringResource(R.string.terms_changes_title)}\n")
        append("${stringResource(R.string.terms_changes_body)}\n\n")

        append("### ${stringResource(R.string.terms_privacy_title)}\n")
        append("${stringResource(R.string.terms_privacy_body)} **https://github.com/3zpnix/WriteOn/**.\n\n")

        append("### ${stringResource(R.string.terms_contact_title)}\n")
        append("${stringResource(R.string.terms_contact_body)} ${ConnectionConst.SUPPORT_MAIL}.\n\n")

        append("*${stringResource(R.string.terms_effective_date)}: ${ConnectionConst.TERMS_EFFECTIVE_DATE}\n")
    }
}