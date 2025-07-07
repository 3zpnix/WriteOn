package com.ezpnix.writeon.presentation.screens.settings

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class WebDAVBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val BACKUP_FILENAME = "notes-backup-encrypted.zip"
    private val client = OkHttpClient()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("webdav_prefs", Context.MODE_PRIVATE)
            val webdavUrl = prefs.getString("webdav_url", "") ?: return@withContext Result.failure()
            val username = prefs.getString("webdav_username", "") ?: return@withContext Result.failure()
            val password = prefs.getString("webdav_password", "") ?: return@withContext Result.failure()

            val backupFile = File(applicationContext.cacheDir, BACKUP_FILENAME)
            if (!backupFile.exists()) return@withContext Result.failure()

            val request = Request.Builder()
                .url(webdavUrl)
                .put(backupFile.asRequestBody())
                .header("Authorization", Credentials.basic(username, password))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}