package com.ezpnix.writeon.presentation.screens.settings

import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val stat = StatFs(Environment.getDataDirectory().absolutePath)
    val totalStorage = formatStorage(stat.totalBytes)
    val availableStorage = formatStorage(stat.availableBytes)

    val deviceInfo = listOf(
        "Manufacturer" to Build.MANUFACTURER,
        "Model" to Build.MODEL,
        "Brand" to Build.BRAND,
        "Device" to Build.DEVICE,
        "Board" to Build.BOARD,
        "Hardware" to Build.HARDWARE,
        "CPU ABI" to Build.SUPPORTED_ABIS.joinToString(),
        "Android Version" to Build.VERSION.RELEASE,
        "SDK Level" to Build.VERSION.SDK_INT.toString(),
        "Security Patch" to (Build.VERSION.SECURITY_PATCH ?: "Unknown"),
        "Storage" to "$availableStorage free / $totalStorage total",

        "Display" to "${Build.DISPLAY}",
        "Host" to "${Build.HOST}",
        "Bootloader" to "${Build.BOOTLOADER}",
        "Fingerprint" to "${Build.FINGERPRINT}",
        "Build ID" to "${Build.ID}",
        "Build Time" to formatBuildTime(Build.TIME),
        "Radio Version" to (Build.getRadioVersion() ?: "Unknown")
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        deviceInfo.forEach { (label, value) ->
                            Text(
                                text = "$label: $value",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "✦ App Developed By: @3zpnix",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    )
}

fun formatStorage(bytes: Long): String {
    val df = DecimalFormat("#.##")
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "${df.format(gb)} GB"
        mb >= 1 -> "${df.format(mb)} MB"
        else -> "${df.format(kb)} KB"
    }
}

fun formatBuildTime(time: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(time))
}

