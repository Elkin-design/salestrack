package org.salestrack.app.domain.usecase.backup

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.BackupArtifact
import org.salestrack.app.domain.model.BackupPayload
import org.salestrack.app.domain.repository.BackupRepository
import org.salestrack.app.domain.repository.CategoryRepository
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.NotificationRepository
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.repository.SettingsRepository

class CreateBackupUseCase(
    private val saleRepository: SaleRepository,
    private val inventoryRepository: InventoryRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationRepository: NotificationRepository,
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(): AppResult<BackupArtifact> {
        val sales = saleRepository.observeSales().first().filter { !it.isDeleted }
        val products = inventoryRepository.observeProducts().first()
        val categories = categoryRepository.observeCategories().first().filter { it.isActive }
        val settings = settingsRepository.observeSettings().first()
        val notifications = notificationRepository.observeSettings().first()

        return backupRepository.createBackup(
            BackupPayload(
                salesCount = sales.size,
                productsCount = products.size,
                categoriesCount = categories.size,
                settingsSnapshot = settings,
                notificationSettings = notifications,
            ),
        )
    }
}
