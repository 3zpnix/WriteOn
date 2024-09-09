package com.ezpnix.writeon.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.ezpnix.writeon.data.repository.SettingsRepositoryImpl
import com.ezpnix.writeon.presentation.components.registerGalleryObserver
import com.ezpnix.writeon.presentation.navigation.AppNavHost
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import com.ezpnix.writeon.presentation.theme.LeafNotesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepositoryImpl: SettingsRepositoryImpl

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        if (settingsViewModel.settings.value.isBiometricEnabled) {
            showBiometricPrompt()
        }

        setContent {
            val noteId = intent?.getIntExtra("noteId", -1) ?: -1
            val navController = rememberNavController()

            registerGalleryObserver(this)

            LeafNotesTheme(settingsViewModel) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    AppNavHost(settingsViewModel, noteId = noteId, navController = navController)
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use fingerprint to unlock the application")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onDestroy() {
        super.onDestroy()
        setContent {
            val settingsViewModel: SettingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
            settingsViewModel.update(settingsViewModel.settings.value.copy(vaultEnabled = false))
        }
    }
}
