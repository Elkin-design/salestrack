package org.salestrack.app.presentation.app.di

import org.koin.dsl.module
import org.salestrack.app.core.dispatcher.DefaultDispatcherProvider
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.utils.SystemTimeProvider
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.export.BasicPdfExportAdapter
import org.salestrack.app.data.export.SpreadsheetXmlExcelExportAdapter
import org.salestrack.app.data.mock.MockCategoryFactory
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.mock.MockNotificationSettingsFactory
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.mock.MockSettingsFactory
import org.salestrack.app.data.mock.MockTeamFactory
import org.salestrack.app.data.repository.FakeBackupRepository
import org.salestrack.app.data.repository.FakeCategoryRepository
import org.salestrack.app.data.repository.FirestoreCategoryRepository
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.repository.FakeNotificationRepository
import org.salestrack.app.data.repository.FakePrintRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.FakeSettingsRepository
import org.salestrack.app.data.repository.FakeTeamRepository
import org.salestrack.app.data.repository.RealInventoryRepository
import org.salestrack.app.data.repository.RealExportRepository
import org.salestrack.app.data.repository.RealSaleRepository
import org.salestrack.app.data.source.FirestoreInventoryDataSource
import org.salestrack.app.data.source.FirestoreSaleDataSource
import org.salestrack.app.data.source.InventoryDataSource
import org.salestrack.app.data.source.InMemoryCategoryDataSource
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.data.source.InMemoryNotificationSettingsDataSource
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.data.source.InMemorySettingsDataSource
import org.salestrack.app.data.source.InMemoryTeamDataSource
import org.salestrack.app.data.source.SaleDataSource
import org.salestrack.app.domain.repository.BackupRepository
import org.salestrack.app.domain.repository.CategoryRepository
import org.salestrack.app.domain.repository.ExportRepository
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.NotificationRepository
import org.salestrack.app.domain.repository.PrintRepository
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.repository.SettingsRepository
import org.salestrack.app.domain.repository.TeamRepository
import org.salestrack.app.domain.usecase.backup.CreateBackupUseCase
import org.salestrack.app.domain.usecase.category.CreateCategoryUseCase
import org.salestrack.app.domain.usecase.category.DeleteCategoryUseCase
import org.salestrack.app.domain.usecase.category.ObserveCategoriesUseCase
import org.salestrack.app.domain.usecase.category.UpdateCategoryUseCase
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.export.ExportCsvUseCase
import org.salestrack.app.domain.usecase.export.ExportExcelUseCase
import org.salestrack.app.domain.usecase.export.ExportPdfUseCase
import org.salestrack.app.domain.usecase.inventory.AddProductUseCase
import org.salestrack.app.domain.usecase.inventory.AdjustStockUseCase
import org.salestrack.app.domain.usecase.inventory.DeleteProductUseCase
import org.salestrack.app.domain.usecase.inventory.EditProductUseCase
import org.salestrack.app.domain.usecase.inventory.ExportCatalogCsvUseCase
import org.salestrack.app.domain.usecase.inventory.ExportCatalogExcelUseCase
import org.salestrack.app.domain.usecase.inventory.FilterProductsUseCase
import org.salestrack.app.domain.usecase.inventory.GetLowStockProductsUseCase
import org.salestrack.app.domain.usecase.inventory.ImportCatalogCsvUseCase
import org.salestrack.app.domain.usecase.notification.ObserveNotificationSettingsUseCase
import org.salestrack.app.domain.usecase.notification.UpdateNotificationSettingsUseCase
import org.salestrack.app.domain.usecase.print.PrintReportUseCase
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
import org.salestrack.app.domain.repository.AuthRepository
import org.salestrack.app.data.repository.FirebaseAuthRepository
import org.salestrack.app.domain.usecase.auth.SignInWithGoogleUseCase
import org.salestrack.app.domain.usecase.auth.SignOutUseCase
import org.salestrack.app.domain.usecase.auth.GetAuthStateUseCase
import org.salestrack.app.presentation.feature.auth.AuthViewModel
import org.salestrack.app.core.utils.platformGoogleSignInNavigator
import org.salestrack.app.presentation.app.AppContainer

fun appModule(config: EnvironmentConfig) = module {
    single { config }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<TimeProvider> { SystemTimeProvider() }

    single { InMemorySaleDataSource(MockSalesFactory.create(get())) }
    single {
        InMemoryInventoryDataSource(
            MockInventoryFactory.create(),
        )
    }
    single { InMemoryCategoryDataSource(MockCategoryFactory.create(get())) }
    single { InMemorySettingsDataSource(MockSettingsFactory.create(get())) }
    single { InMemoryNotificationSettingsDataSource(MockNotificationSettingsFactory.create(get())) }
    single { InMemoryTeamDataSource(MockTeamFactory.create()) }

    single<SaleRepository> {
        RealSaleRepository(dataSource = get())
    }

    single<SaleDataSource> {
        FirestoreSaleDataSource(
            timeProvider = get(),
        )
    }

    single<InventoryRepository> {
        RealInventoryRepository(dataSource = get())
    }

    single<InventoryDataSource> {
        FirestoreInventoryDataSource(
            timeProvider = get(),
        )
    }
    single<CategoryRepository> {
        FirestoreCategoryRepository(timeProvider = get())
    }
    single<SettingsRepository> { FakeSettingsRepository(get()) }
    single<NotificationRepository> { FakeNotificationRepository(get()) }
    single<ExportRepository> {
        RealExportRepository(
            pdfAdapter = BasicPdfExportAdapter(),
            excelAdapter = SpreadsheetXmlExcelExportAdapter(),
            fileSaver = org.salestrack.app.core.utils.platformFileSaver,
        )
    }
    single<PrintRepository> { FakePrintRepository() }
    single<BackupRepository> { FakeBackupRepository() }
    single<TeamRepository> { FakeTeamRepository(get()) }
    single<AuthRepository> { FirebaseAuthRepository() }

    single { SignInWithGoogleUseCase(get()) }
    single { SignOutUseCase(get()) }
    single { GetAuthStateUseCase(get()) }

    factory { 
        AuthViewModel(
            signInWithGoogleUseCase = get(),
            signOutUseCase = get(),
            getAuthStateUseCase = get(),
            googleSignInNavigator = platformGoogleSignInNavigator
        )
    }

    single { AddSaleUseCase(get(), get(), get()) }
    single { UpdateSaleUseCase(get()) }
    single { DeleteSaleUseCase(get()) }
    single { FilterSalesUseCase() }
    single { BuildDashboardSummaryUseCase() }
    single { GetDailyReportUseCase(get()) }
    single { GetCustomRangeReportUseCase(get()) }
    single { GetPeriodReportUseCase(get()) }
    single { GetTeamSalesUseCase() }
    single { InviteMemberUseCase(get()) }
    single { GetRolePermissionsUseCase() }
    single { AddProductUseCase(get(), get()) }
    single { EditProductUseCase(get(), get()) }
    single { DeleteProductUseCase(get()) }
    single { FilterProductsUseCase() }
    single { AdjustStockUseCase(get()) }
    single { GetLowStockProductsUseCase(get()) }
    single { ImportCatalogCsvUseCase(get()) }
    single { ExportCatalogCsvUseCase(get()) }
    single { ExportCatalogExcelUseCase(get()) }
    single { ObserveCategoriesUseCase(get()) }
    single { CreateCategoryUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }
    single { ObserveSettingsUseCase(get()) }
    single { UpdateSettingsUseCase(get(), get()) }
    single { ObserveNotificationSettingsUseCase(get()) }
    single { UpdateNotificationSettingsUseCase(get(), get()) }
    single { ExportPdfUseCase(get(), get()) }
    single { ExportExcelUseCase(get(), get()) }
    single { ExportCsvUseCase(get(), get()) }
    single { PrintReportUseCase(get(), get()) }
    single {
        CreateBackupUseCase(
            saleRepository = get(),
            inventoryRepository = get(),
            categoryRepository = get(),
            settingsRepository = get(),
            notificationRepository = get(),
            backupRepository = get(),
        )
    }

    single {
        AppContainer(
            dispatcherProvider = get(),
            timeProvider = get(),
            saleRepository = get(),
            inventoryRepository = get(),
            categoryRepository = get(),
            settingsRepository = get(),
            notificationRepository = get(),
            exportRepository = get(),
            printRepository = get(),
            backupRepository = get(),
            teamRepository = get(),
            addSaleUseCase = get(),
            updateSaleUseCase = get(),
            deleteSaleUseCase = get(),
            filterSalesUseCase = get(),
            buildDashboardSummaryUseCase = get(),
            getDailyReportUseCase = get(),
            getCustomRangeReportUseCase = get(),
            getPeriodReportUseCase = get(),
            getTeamSalesUseCase = get(),
            inviteMemberUseCase = get(),
            getRolePermissionsUseCase = get(),
            addProductUseCase = get(),
            editProductUseCase = get(),
            deleteProductUseCase = get(),
            filterProductsUseCase = get(),
            adjustStockUseCase = get(),
            getLowStockProductsUseCase = get(),
            importCatalogCsvUseCase = get(),
            exportCatalogCsvUseCase = get(),
            exportCatalogExcelUseCase = get(),
            observeCategoriesUseCase = get(),
            createCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get(),
            observeSettingsUseCase = get(),
            updateSettingsUseCase = get(),
            observeNotificationSettingsUseCase = get(),
            updateNotificationSettingsUseCase = get(),
            exportPdfUseCase = get(),
            exportExcelUseCase = get(),
            exportCsvUseCase = get(),
            printReportUseCase = get(),
            createBackupUseCase = get(),
            authViewModel = get(),
            getAuthStateUseCase = get(),
            signOutUseCase = get(),
        )
    }
}
