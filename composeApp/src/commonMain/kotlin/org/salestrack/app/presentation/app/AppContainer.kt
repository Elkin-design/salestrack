package org.salestrack.app.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.salestrack.app.core.dispatcher.DefaultDispatcherProvider
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.utils.SystemTimeProvider
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.mock.MockSettingsFactory
import org.salestrack.app.data.mock.MockTeamFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.FakeSettingsRepository
import org.salestrack.app.data.repository.FakeTeamRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.data.source.InMemorySettingsDataSource
import org.salestrack.app.data.source.InMemoryTeamDataSource
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.repository.SettingsRepository
import org.salestrack.app.domain.repository.TeamRepository
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.inventory.AddProductUseCase
import org.salestrack.app.domain.usecase.inventory.AdjustStockUseCase
import org.salestrack.app.domain.usecase.inventory.EditProductUseCase
import org.salestrack.app.domain.usecase.inventory.FilterProductsUseCase
import org.salestrack.app.domain.usecase.reports.GetCustomRangeReportUseCase
import org.salestrack.app.domain.usecase.reports.GetDailyReportUseCase
import org.salestrack.app.domain.usecase.reports.GetPeriodReportUseCase
import org.salestrack.app.domain.usecase.sales.AddSaleUseCase
import org.salestrack.app.domain.usecase.sales.DeleteSaleUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.domain.usecase.sales.UpdateSaleUseCase
import org.salestrack.app.domain.usecase.settings.ObserveSettingsUseCase
import org.salestrack.app.domain.usecase.settings.UpdateSettingsUseCase
import org.salestrack.app.domain.usecase.team.GetRolePermissionsUseCase
import org.salestrack.app.domain.usecase.team.GetTeamSalesUseCase
import org.salestrack.app.domain.usecase.team.InviteMemberUseCase

class AppContainer(
    val dispatcherProvider: DispatcherProvider,
    val timeProvider: TimeProvider,
    val saleRepository: SaleRepository,
    val inventoryRepository: InventoryRepository,
    val settingsRepository: SettingsRepository,
    val teamRepository: TeamRepository,
    val addSaleUseCase: AddSaleUseCase,
    val updateSaleUseCase: UpdateSaleUseCase,
    val deleteSaleUseCase: DeleteSaleUseCase,
    val filterSalesUseCase: FilterSalesUseCase,
    val buildDashboardSummaryUseCase: BuildDashboardSummaryUseCase,
    val getDailyReportUseCase: GetDailyReportUseCase,
    val getCustomRangeReportUseCase: GetCustomRangeReportUseCase,
    val getPeriodReportUseCase: GetPeriodReportUseCase,
    val getTeamSalesUseCase: GetTeamSalesUseCase,
    val inviteMemberUseCase: InviteMemberUseCase,
    val getRolePermissionsUseCase: GetRolePermissionsUseCase,
    val addProductUseCase: AddProductUseCase,
    val editProductUseCase: EditProductUseCase,
    val filterProductsUseCase: FilterProductsUseCase,
    val adjustStockUseCase: AdjustStockUseCase,
    val observeSettingsUseCase: ObserveSettingsUseCase,
    val updateSettingsUseCase: UpdateSettingsUseCase,
)

@Composable
fun rememberAppContainer(): AppContainer {
    return remember {
        val dispatchers = DefaultDispatcherProvider()
        val timeProvider = SystemTimeProvider()
        val dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider))
        val repository = FakeSaleRepository(dataSource = dataSource, timeProvider = timeProvider)

        val inventoryDataSource = InMemoryInventoryDataSource(MockInventoryFactory.create())
        val inventoryRepository = FakeInventoryRepository(
            dataSource = inventoryDataSource,
            timeProvider = timeProvider,
        )

        val settingsDataSource = InMemorySettingsDataSource(MockSettingsFactory.create(timeProvider))
        val settingsRepository = FakeSettingsRepository(settingsDataSource)

        val teamDataSource = InMemoryTeamDataSource(MockTeamFactory.create())
        val teamRepository = FakeTeamRepository(teamDataSource)

        AppContainer(
            dispatcherProvider = dispatchers,
            timeProvider = timeProvider,
            saleRepository = repository,
            inventoryRepository = inventoryRepository,
            settingsRepository = settingsRepository,
            teamRepository = teamRepository,
            addSaleUseCase = AddSaleUseCase(repository, inventoryRepository),
            updateSaleUseCase = UpdateSaleUseCase(repository),
            deleteSaleUseCase = DeleteSaleUseCase(repository),
            filterSalesUseCase = FilterSalesUseCase(),
            buildDashboardSummaryUseCase = BuildDashboardSummaryUseCase(),
            getDailyReportUseCase = GetDailyReportUseCase(repository),
            getCustomRangeReportUseCase = GetCustomRangeReportUseCase(repository),
            getPeriodReportUseCase = GetPeriodReportUseCase(repository),
            getTeamSalesUseCase = GetTeamSalesUseCase(),
            inviteMemberUseCase = InviteMemberUseCase(teamRepository),
            getRolePermissionsUseCase = GetRolePermissionsUseCase(),
            addProductUseCase = AddProductUseCase(inventoryRepository),
            editProductUseCase = EditProductUseCase(inventoryRepository),
            filterProductsUseCase = FilterProductsUseCase(),
            adjustStockUseCase = AdjustStockUseCase(inventoryRepository),
            observeSettingsUseCase = ObserveSettingsUseCase(settingsRepository),
            updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository, timeProvider),
        )
    }
}

