package com.kin.easynotes.presentation.screens.settings

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kin.easynotes.core.constant.DatabaseConst
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
            // Path to the directory where the backup will be stored
            val backupFolder = File(context.getExternalFilesDir(null), "BackupFolder")
            if (!backupFolder.exists()) {
                backupFolder.mkdirs() // Create the directory if it doesn't exist
            }

            // Generate the backup file name
            val backupFileName = "${DatabaseConst.NOTES_DATABASE_BACKUP_NAME}-${currentDateTime()}.zip"
            val backupFile = File(backupFolder, backupFileName)

            // Perform the backup operation
            backupDatabase(backupFile, password = "your_password") // Replace with actual password handling

            // If backup is successful, return Result.success()
            Result.success()
        } catch (e: Exception) {
            // If there is an error, return Result.failure()
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun backupDatabase(backupFile: File, password: String?) {
        withContext(Dispatchers.IO) {
            // Path to the database file
            val databaseFile = applicationContext.getDatabasePath(DatabaseConst.NOTES_DATABASE_FILE_NAME)

            // Create a temporary zip file
            val tempZipFile = File.createTempFile("backup", ".zip", applicationContext.cacheDir)

            // Zip the database file
            ZipOutputStream(FileOutputStream(tempZipFile)).use { zipOutputStream ->
                FileInputStream(databaseFile).use { inputStream ->
                    val zipEntry = ZipEntry(databaseFile.name)
                    zipOutputStream.putNextEntry(zipEntry)
                    inputStream.copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            }

            val zipData = tempZipFile.readBytes()

            // Encrypt the data if a password is provided
            if (password != null) {
                val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
                val secretKey = generateSecretKey(password, salt)
                val encryptedData = encrypt(zipData, secretKey)
                backupFile.writeBytes(salt + encryptedData)
            } else {
                backupFile.writeBytes(zipData)
            }

            // Delete the temporary zip file
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
        // Generate a timestamp string for the backup file name
        val format = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        return format.format(java.util.Date())
    }
}
