package org.salestrack.app.domain.usecase.backup

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockCategoryFactory
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.mock.MockNotificationSettingsFactory
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.mock.MockSettingsFactory
import org.salestrack.app.data.repository.FakeBackupRepository
import org.salestrack.app.data.repository.FakeCategoryRepository
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.repository.FakeNotificationRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.FakeSettingsRepository
import org.salestrack.app.data.source.InMemoryCategoryDataSource
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.data.source.InMemoryNotificationSettingsDataSource
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.data.source.InMemorySettingsDataSource
import kotlin.test.Test
import kotlin.test.assertTrue

class CreateBackupUseCaseTest {

    @Test
    fun should_create_backup_with_all_datasets() = runTest {
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val inventoryRepository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = timeProvider,
        )
        val categoryRepository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val settingsRepository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(MockSettingsFactory.create(timeProvider)),
        )
        val notificationRepository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = CreateBackupUseCase(
            saleRepository = saleRepository,
            inventoryRepository = inventoryRepository,
            categoryRepository = categoryRepository,
            settingsRepository = settingsRepository,
            notificationRepository = notificationRepository,
            backupRepository = FakeBackupRepository(),
        )

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.jsonPreview.contains("sales="))
    }
}
