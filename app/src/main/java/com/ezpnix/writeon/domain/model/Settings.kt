package com.ezpnix.writeon.domain.model

data class Settings(
    val viewMode: Boolean = true,
    val automaticTheme: Boolean = false,
    val darkTheme: Boolean = true,
    var dynamicTheme: Boolean = false,
    var amoledTheme: Boolean = false,
    var minimalisticMode: Boolean = false,
    var extremeAmoledMode: Boolean = false,
    var isMarkdownEnabled: Boolean = true,
    var screenProtection: Boolean = false,
    var encryptBackup: Boolean = false,
    var sortDescending: Boolean = true,
    var vaultSettingEnabled: Boolean = false,
    var vaultEnabled: Boolean = false,
    var editMode: Boolean = false,
    var showOnlyTitle: Boolean = false,
    var termsOfService: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val fontSize: Float = 16f,

    var cornerRadius: Int = 32,
)
