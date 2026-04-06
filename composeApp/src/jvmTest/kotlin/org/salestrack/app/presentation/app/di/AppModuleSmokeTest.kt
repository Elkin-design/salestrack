package org.salestrack.app.presentation.app.di

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.salestrack.app.presentation.app.AppContainer
import kotlin.test.assertEquals
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleSmokeTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun should_resolve_app_container_and_core_dependencies() {
        stopKoin()
        val app = startKoin {
            modules(appModule(EnvironmentConfig(backendProvider = BackendProvider.MOCK)))
        }

        val container: AppContainer = app.koin.get()

        assertNotNull(container.dispatcherProvider)
        assertNotNull(container.timeProvider)
        assertNotNull(container.saleRepository)
        assertNotNull(container.inventoryRepository)
        assertNotNull(container.categoryRepository)
        assertNotNull(container.settingsRepository)
        assertNotNull(container.notificationRepository)
        assertNotNull(container.exportRepository)
        assertNotNull(container.printRepository)
        assertNotNull(container.backupRepository)
        assertNotNull(container.teamRepository)
        assertNotNull(container.buildDashboardSummaryUseCase)
        assertNotNull(container.filterSalesUseCase)
    }

    @Test
    fun should_resolve_sales_flow_when_backend_is_firestore_stub() {
        stopKoin()
        val app = startKoin {
            modules(appModule(EnvironmentConfig(backendProvider = BackendProvider.FIRESTORE_STUB)))
        }

        val container: AppContainer = app.koin.get()
        assertNotNull(container.saleRepository)

        val sales = runBlocking {
            container.saleRepository.observeSales().first()
        }
        assertEquals(4, sales.size)
    }
}
