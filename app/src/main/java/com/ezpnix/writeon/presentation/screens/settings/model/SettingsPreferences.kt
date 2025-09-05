package com.ezpnix.writeon.presentation.screens.settings.model

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    val dynamicPlaceholder: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLACEHOLDER_KEY] ?: "Simple Notepad"
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

    val visibleFabItems: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[stringSetPreferencesKey("visible_fab_items")] ?: emptySet()
        }

    suspend fun saveVisibleFabItems(items: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[stringSetPreferencesKey("visible_fab_items")] = items
        }
    }

    suspend fun saveColumnsCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[COLUMNS_COUNT_KEY] = count
        }
    }
}
