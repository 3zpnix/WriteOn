package com.ezpnix.oracleswift.presentation.screens.settings

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ezpnix.oracleswift.core.constant.DatabaseConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec

class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        return try {
            val backupFolder = File(context.getExternalFilesDir(null), "BackupFolder")
            if (!backupFolder.exists()) {
                backupFolder.mkdirs()
            }

            val backupFileName = "${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip"
            val backupFile = File(backupFolder, backupFileName)

            // REMINDERS:
            // Perform the backup operation
            // Replace with actual password handling
            // If backup is successful, return Result.success()
            // If there is an error, return Result.failure()
            backupDatabase(backupFile, password = "your_password")
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun backupDatabase(backupFile: File, password: String?) {
        withContext(Dispatchers.IO) {
            val databaseFile = applicationContext.getDatabasePath(DatabaseConst.NOTES_DATABASE_FILE_NAME)

            val tempZipFile = File.createTempFile("backup", ".zip", applicationContext.cacheDir)

            ZipOutputStream(FileOutputStream(tempZipFile)).use { zipOutputStream ->
                FileInputStream(databaseFile).use { inputStream ->
                    val zipEntry = ZipEntry(databaseFile.name)
                    zipOutputStream.putNextEntry(zipEntry)
                    inputStream.copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            }

            val zipData = tempZipFile.readBytes()

            if (password != null) {
                val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
                val secretKey = generateSecretKey(password, salt)
                val encryptedData = encrypt(zipData, secretKey)
                backupFile.writeBytes(salt + encryptedData)
            } else {
                backupFile.writeBytes(zipData)
            }

            tempZipFile.delete()
        }
    }

    private fun generateSecretKey(password: String, salt: ByteArray): SecretKey {
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData
    }

    private fun currentDateTime(): String {
        val format = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        return format.format(java.util.Date())
    }
}
