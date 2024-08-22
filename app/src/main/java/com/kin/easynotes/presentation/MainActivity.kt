package com.kin.easynotes.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.kin.easynotes.data.repository.SettingsRepositoryImpl
import com.kin.easynotes.presentation.components.registerGalleryObserver
import com.kin.easynotes.presentation.navigation.AppNavHost
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.theme.LeafNotesTheme
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

        // Initialize the ViewModel using ViewModelProvider
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        // Check if biometric authentication is enabled and prompt the user if necessary
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
                    // Proceed with the app
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle the error (e.g., exit app or show a message)
                    finish() // Close the app or handle as needed
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle failed authentication attempt
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
