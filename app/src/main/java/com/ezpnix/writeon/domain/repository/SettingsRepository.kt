package com.ezpnix.writeon.domain.repository

interface SettingsRepository {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?

    suspend fun putInt(key: String, value: Int)
    suspend fun getInt(key: String): Int?

    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String): Boolean?

    suspend fun putFloat(key: String, value: Float)
    suspend fun getFloat(key: String): Float?

    suspend fun getEveryNotesWidget(): List<Pair<Int, Int>>
}
