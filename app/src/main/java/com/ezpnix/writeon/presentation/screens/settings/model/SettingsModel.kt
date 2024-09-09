package com.ezpnix.writeon.presentation.screens.settings.model

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ezpnix.writeon.BuildConfig
import com.ezpnix.writeon.R
import com.ezpnix.writeon.data.repository.ImportExportRepository
import com.ezpnix.writeon.data.repository.BackupResult
import com.ezpnix.writeon.domain.model.Settings
import com.ezpnix.writeon.domain.usecase.ImportExportUseCase
import com.ezpnix.writeon.domain.usecase.ImportResult
import com.ezpnix.writeon.domain.usecase.NoteUseCase
import com.ezpnix.writeon.domain.usecase.SettingsUseCase
import com.ezpnix.writeon.presentation.screens.settings.BackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backup: ImportExportRepository,
    private val settingsUseCase: SettingsUseCase,
    val noteUseCase: NoteUseCase,
    private val importExportUseCase: ImportExportUseCase, // AA1
    @ApplicationContext private val context: Context
) : ViewModel() {
    val databaseUpdate = mutableStateOf(false)
    var password : String? = null

    private val _settings = mutableStateOf(Settings())
    var settings: State<Settings> = _settings

    init {
        viewModelScope.launch {
            loadSettings()
            if (_settings.value.autoBackupEnabled) run {
                startAutoBackup(context)
            }
        }
    }

    private suspend fun loadSettings() {
        val loadedSettings = runBlocking(Dispatchers.IO) {
            settingsUseCase.loadSettingsFromRepository()
        }
        _settings.value = loadedSettings
    }

    fun update(newSettings: Settings) {
        _settings.value = newSettings.copy()
        viewModelScope.launch {
            settingsUseCase.saveSettingsToRepository(newSettings)
            if (newSettings.autoBackupEnabled) {
                startAutoBackup(context)
            } else {
                stopAutoBackup(context)
            }
        }
    }

    private fun startAutoBackup(context: Context) {
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AutoBackup",
            ExistingPeriodicWorkPolicy.REPLACE,
            backupRequest
        )
    }

    private fun stopAutoBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("AutoBackup")
    }


    fun onExportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            val result = backup.exportBackup(uri, password)
            handleBackupResult(result, context)
            databaseUpdate.value = true
        }
    }

    fun onImportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            val result = backup.importBackup(uri, password)
            handleBackupResult(result, context)
            databaseUpdate.value = true
        }
    }

    // Taken from: https://stackoverflow.com/questions/74114067/get-list-of-locales-from-locale-config-in-android-13
    private fun getLocaleListFromXml(context: Context): LocaleListCompat {
        val tagsList = mutableListOf<CharSequence>()
        try {
            val xpp: XmlPullParser = context.resources.getXml(R.xml.locales_config)
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "locale") {
                        tagsList.add(xpp.getAttributeValue(0))
                    }
                }
                xpp.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
    }

    fun getSupportedLanguages(context: Context): Map<String, String> {
        val localeList = getLocaleListFromXml(context)
        val map = mutableMapOf<String, String>()

        for (a in 0 until localeList.size()) {
            localeList[a].let {
                it?.let { it1 -> map.put(it1.getDisplayName(it), it.toLanguageTag()) }
            }
        }
        return map
    }

    private fun handleBackupResult(result: BackupResult, context: Context) {
        when (result) {
            is BackupResult.Success -> showToast("Backup Restored", context)
            is BackupResult.Error -> showToast("Error", context)
            BackupResult.BadPassword -> showToast(context.getString(R.string.database_restore_error), context)
        }
    }

    // AA2
    private fun handleImportResult(result: ImportResult, context: Context) {
        when (result.successful) {
            result.total -> {showToast(context.getString(R.string.file_import_success), context)}
            0 -> {showToast(context.getString(R.string.file_import_error), context)}
            else -> {showToast(context.getString(R.string.file_import_partial_error), context)}
        }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val version: String = BuildConfig.VERSION_NAME
    val build: String = BuildConfig.BUILD_TYPE
}