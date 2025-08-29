package com.ezpnix.writeon.presentation.screens.settings.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ezpnix.writeon.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PLACEHOLDER_KEY = stringPreferencesKey("search_placeholder")
        private val COLUMNS_COUNT_KEY = intPreferencesKey("columns_count")
        private const val DEFAULT_COLUMNS_COUNT = 2
    }

    val msg = context.getString(R.string.home_app_subtitle)

    val dynamicPlaceholder: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLACEHOLDER_KEY] ?: msg
        }

    suspend fun savePlaceholder(placeholder: String) {
        context.dataStore.edit { preferences ->
            preferences[PLACEHOLDER_KEY] = placeholder
        }
    }

    val columnsCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            val raw = preferences[COLUMNS_COUNT_KEY] ?: DEFAULT_COLUMNS_COUNT
            if (raw > 5) DEFAULT_COLUMNS_COUNT else raw
        }

    suspend fun saveColumnsCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[COLUMNS_COUNT_KEY] = count
        }
    }
}
