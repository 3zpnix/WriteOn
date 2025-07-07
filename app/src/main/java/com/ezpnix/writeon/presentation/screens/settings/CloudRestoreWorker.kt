package com.ezpnix.writeon.presentation.screens.settings

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ezpnix.writeon.core.constant.DatabaseConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.spec.KeySpec

class WebDAVRestoreWorker(
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

            val request = Request.Builder()
                .url(webdavUrl)
                .header("Authorization", Credentials.basic(username, password))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.retry()

            val tempEncryptedFile = File(applicationContext.cacheDir, BACKUP_FILENAME)
            response.body?.byteStream()?.use { input ->
                FileOutputStream(tempEncryptedFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure()

            val settingsPrefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val userPassword = settingsPrefs.getString("encryption_password", "") ?: return@withContext Result.failure()
            if (userPassword.isBlank()) return@withContext Result.failure()

            val encryptedBytes = tempEncryptedFile.readBytes()
            val decryptedBytes = decrypt(encryptedBytes, userPassword) ?: return@withContext Result.failure()

            val decryptedZipFile = File.createTempFile("decrypted_backup", ".zip", applicationContext.cacheDir)
            decryptedZipFile.writeBytes(decryptedBytes)

            unzipAndReplaceDatabase(decryptedZipFile)

            Result.success()
        } catch (e: IOException) {
            e.printStackTrace()
            Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun decrypt(encrypted: ByteArray, password: String): ByteArray? {
        return try {
            val salt = encrypted.copyOfRange(0, 16)
            val iv = encrypted.copyOfRange(16, 32)
            val data = encrypted.copyOfRange(32, encrypted.size)

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun unzipAndReplaceDatabase(zipFile: File) {
        val dbFile = applicationContext.getDatabasePath(DatabaseConst.NOTES_DATABASE_FILE_NAME)
        val tempDir = File(applicationContext.cacheDir, "unzipped_restore").apply { mkdirs() }

        ZipInputStream(zipFile.inputStream()).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                val outFile = File(tempDir, entry.name)
                FileOutputStream(outFile).use { output -> zipStream.copyTo(output) }

                if (entry.name == dbFile.name) {
                    dbFile.delete()
                    outFile.copyTo(dbFile, overwrite = true)
                }

                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
    }
}