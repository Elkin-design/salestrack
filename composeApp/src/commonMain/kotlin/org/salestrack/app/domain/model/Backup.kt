package org.salestrack.app.domain.model

data class BackupPayload(
    val salesCount: Int,
    val productsCount: Int,
    val categoriesCount: Int,
    val settingsSnapshot: AppSettings,
    val notificationSettings: NotificationSettings,
)

data class BackupArtifact(
    val fileName: String,
    val jsonPreview: String,
    val excelPreview: String,
)
