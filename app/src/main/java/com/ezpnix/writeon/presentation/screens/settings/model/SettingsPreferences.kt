package com.ezpnix.writeon.presentation.screens.settings.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PLACEHOLDER_KEY = stringPreferencesKey("search_placeholder")
    }

    val dynamicPlaceholder: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLACEHOLDER_KEY] ?: "Simple Notepad"
        }

    suspend fun savePlaceholder(placeholder: String) {
        context.dataStore.edit { preferences ->
            preferences[PLACEHOLDER_KEY] = placeholder
        }
    }
}
