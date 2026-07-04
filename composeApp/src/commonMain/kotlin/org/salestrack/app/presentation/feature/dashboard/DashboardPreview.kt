package org.salestrack.app.presentation.feature.dashboard

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.flowOf
import org.salestrack.app.core.dispatcher.DefaultDispatcherProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.SystemTimeProvider
import org.salestrack.app.domain.model.*
import org.salestrack.app.domain.repository.*
import org.salestrack.app.domain.usecase.backup.CreateBackupUseCase
import org.salestrack.app.domain.usecase.category.*
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.export.*
import org.salestrack.app.domain.usecase.inventory.*
import org.salestrack.app.domain.usecase.notification.*
import org.salestrack.app.domain.usecase.print.PrintReportUseCase
import org.salestrack.app.domain.usecase.reports.*
import org.salestrack.app.domain.usecase.sales.*
import org.salestrack.app.domain.usecase.settings.*
import org.salestrack.app.domain.usecase.team.*
import org.salestrack.app.data.source.InventoryDataSource
import org.salestrack.app.data.source.SaleDataSource
import org.salestrack.app.core.utils.GoogleSignInNavigator
import org.salestrack.app.presentation.feature.auth.AuthViewModel
import org.salestrack.app.domain.usecase.auth.SignInWithGoogleUseCase
import org.salestrack.app.domain.usecase.auth.SignOutUseCase
import org.salestrack.app.domain.usecase.auth.GetAuthStateUseCase
import org.salestrack.app.domain.usecase.auth.UpdateDisplayNameUseCase
import org.salestrack.app.presentation.app.AppContainer

@Preview
@Composable
private fun DashboardLoadingPreview() {
    val snackbarHost = remember { SnackbarHostState() }
    DashboardScreen(
        uiState = DashboardUiState(isLoading = true),
        snackbarHostState = snackbarHost,
        onRefresh = {},
        onNavigateToReports = {},
        onNavigateToExport = {},
        onDismissExportModal = {},
        onEvent = {},
        container = createPreviewContainer(),
    )
}

@Preview
@Composable
private fun DashboardErrorPreview() {
    val snackbarHost = remember { SnackbarHostState() }
    DashboardScreen(
        uiState = DashboardUiState(
            isLoading = false,
            errorMessage = "No fue posible cargar el dashboard",
        ),
        snackbarHostState = snackbarHost,
        onRefresh = {},
        onNavigateToReports = {},
        onNavigateToExport = {},
        onDismissExportModal = {},
        onEvent = {},
        container = createPreviewContainer(),
    )
}

@Preview
@Composable
private fun DashboardEmptyPreview() {
    val snackbarHost = remember { SnackbarHostState() }
    DashboardScreen(
        uiState = DashboardUiState(
            isLoading = false,
            summary = DashboardSummary(
                totalSoldToday = 0.0,
                transactionCountToday = 0,
                topProductToday = "Sin ventas",
                syncStatus = "Sincronizado",
            ),
            recentSales = emptyList(),
        ),
        snackbarHostState = snackbarHost,
        onRefresh = {},
        onNavigateToReports = {},
        onNavigateToExport = {},
        onDismissExportModal = {},
        onEvent = {},
        container = createPreviewContainer(),
    )
}

/**
 * Creates a stubbed version of AppContainer for Compose Previews.
 */
private fun createPreviewContainer(): AppContainer {
    val dispatcherProvider = DefaultDispatcherProvider()
    val timeProvider = SystemTimeProvider()

    // Mock Repositories/DataSources
    val saleRepo = object : SaleRepository, SaleDataSource {
        override fun observeSales() = flowOf(emptyList<Sale>())
        override suspend fun addSale(input: NewSaleInput) = AppResult.Success(Sale(id = "", createdAtMillis = 0L, sellerName = ""))
        override suspend fun updateSale(sale: Sale) = AppResult.Success(sale)
        override suspend fun softDeleteSale(saleId: String) = AppResult.Success(Unit)
        override suspend fun clearAllSales() = AppResult.Success(Unit)
    }

    val inventoryRepo = object : InventoryRepository, InventoryDataSource {
        override fun observeProducts() = flowOf(emptyList<Product>())
        override fun observeStockMovements(productId: String?) = flowOf(emptyList<StockMovement>())
        override suspend fun addProduct(input: NewProductInput) = AppResult.Success(Product("", "", "", 0.0, "", null, "", 0, 0, true))
        override suspend fun updateProduct(product: Product) = AppResult.Success(product)
        override suspend fun adjustStock(productId: String, quantityDelta: Int, reason: String, type: StockAdjustmentType, sellerName: String?, platform: String?) = AppResult.Success(Product("", "", "", 0.0, "", null, "", 0, 0, true))
        override suspend fun deductStock(productId: String, quantity: Int, reason: String, sellerName: String?, platform: String?) = AppResult.Success(Product("", "", "", 0.0, "", null, "", 0, 0, true))
        override suspend fun importCatalogCsv(csvContent: String) = AppResult.Success(CatalogImportResult(0, 0, 0, emptyList()))
        override suspend fun exportCatalogCsv() = AppResult.Success(CatalogExportFile("", "", ""))
        override suspend fun exportCatalogExcel() = AppResult.Success(CatalogExportFile("", "", ""))
        override suspend fun deleteProduct(productId: String) = AppResult.Success(Unit)
        override suspend fun getLowStockProducts() = AppResult.Success(emptyList<Product>())
    }

    val categoryRepo = object : CategoryRepository {
        override fun observeCategories() = flowOf(emptyList<Category>())
        override suspend fun createCategory(name: String, colorHex: String) = AppResult.Success(Category("", name, colorHex, true, 0L))
        override suspend fun updateCategory(category: Category) = AppResult.Success(category)
        override suspend fun deleteCategory(categoryId: String) = AppResult.Success(Unit)
    }

    val settingsRepo = object : SettingsRepository {
        override fun observeSettings() = flowOf(AppSettings(CurrencyCode.COP, "", "", AppThemeMode.System, 1.0f, 0L))
        override suspend fun updateSettings(settings: AppSettings) = AppResult.Success(settings)
    }

    val notificationRepo = object : NotificationRepository {
        override fun observeSettings() = flowOf(NotificationSettings(false, 0, 0, 0L))
        override suspend fun updateSettings(settings: NotificationSettings) = AppResult.Success(settings)
    }

    val exportRepo = object : ExportRepository {
        override suspend fun exportReport(payload: ExportReportPayload, format: ExportFormat, destination: ExportDestination) = 
            AppResult.Success(ExportArtifact("", "", destination, ""))
    }

    val printRepo = object : PrintRepository {
        override suspend fun printReport(payload: ExportReportPayload) = AppResult.Success(Unit)
    }

    val backupRepo = object : BackupRepository {
        override suspend fun createBackup(payload: BackupPayload) = AppResult.Success(BackupArtifact("", "", ""))
    }

    val teamRepo = object : TeamRepository {
        override fun observeMembers() = flowOf(emptyList<TeamMember>())
        override suspend fun inviteMember(fullName: String, email: String, role: UserRole) = 
            AppResult.Success(TeamMember("", fullName, email, role, true))
        override suspend fun updateMemberRole(memberId: String, role: UserRole) = 
            AppResult.Success(TeamMember(memberId, "", "", role, true))
        override suspend fun removeMember(memberId: String) = AppResult.Success(Unit)
    }

    val authRepo = object : AuthRepository {
        override fun observeAuthState() = flowOf(null)
        override suspend fun signInWithGoogle(idToken: String) = AppResult.Failure(Exception("Not implemented"))
        override suspend fun signOut() = AppResult.Success(Unit)
        override fun getCurrentUser() = null
        override suspend fun updateDisplayName(name: String) = AppResult.Success(Unit)
    }

    val googleNavigator = object : GoogleSignInNavigator {
        override fun signIn(onResult: (String?, String?) -> Unit) { onResult(null, "Not implemented") }
        override fun signOut(onComplete: () -> Unit) { onComplete() }
    }

    // UseCases
    val exportPdf = ExportPdfUseCase(saleRepo, exportRepo)
    val exportExcel = ExportExcelUseCase(saleRepo, exportRepo)
    val exportCsv = ExportCsvUseCase(saleRepo, exportRepo)

    return AppContainer(
        dispatcherProvider = dispatcherProvider,
        timeProvider = timeProvider,
        saleRepository = saleRepo,
        inventoryRepository = inventoryRepo,
        categoryRepository = categoryRepo,
        settingsRepository = settingsRepo,
        notificationRepository = notificationRepo,
        exportRepository = exportRepo,
        printRepository = printRepo,
        backupRepository = backupRepo,
        teamRepository = teamRepo,
        addSaleUseCase = AddSaleUseCase(saleRepo, inventoryRepo, categoryRepo),
        updateSaleUseCase = UpdateSaleUseCase(saleRepo),
        deleteSaleUseCase = DeleteSaleUseCase(saleRepo),
        filterSalesUseCase = FilterSalesUseCase(),
        buildDashboardSummaryUseCase = BuildDashboardSummaryUseCase(),
        getDailyReportUseCase = GetDailyReportUseCase(saleRepo),
        getCustomRangeReportUseCase = GetCustomRangeReportUseCase(saleRepo),
        getPeriodReportUseCase = GetPeriodReportUseCase(saleRepo),
        getTeamSalesUseCase = GetTeamSalesUseCase(),
        inviteMemberUseCase = InviteMemberUseCase(teamRepo),
        getRolePermissionsUseCase = GetRolePermissionsUseCase(),
        addProductUseCase = AddProductUseCase(inventoryRepo, categoryRepo),
        editProductUseCase = EditProductUseCase(inventoryRepo, categoryRepo),
        deleteProductUseCase = DeleteProductUseCase(inventoryRepo),
        filterProductsUseCase = FilterProductsUseCase(),
        adjustStockUseCase = AdjustStockUseCase(inventoryRepo),
        getLowStockProductsUseCase = GetLowStockProductsUseCase(inventoryRepo),
        importCatalogCsvUseCase = ImportCatalogCsvUseCase(inventoryRepo),
        exportCatalogCsvUseCase = ExportCatalogCsvUseCase(inventoryRepo),
        exportCatalogExcelUseCase = ExportCatalogExcelUseCase(inventoryRepo),
        observeCategoriesUseCase = ObserveCategoriesUseCase(categoryRepo),
        createCategoryUseCase = CreateCategoryUseCase(categoryRepo),
        updateCategoryUseCase = UpdateCategoryUseCase(categoryRepo),
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepo),
        observeSettingsUseCase = ObserveSettingsUseCase(settingsRepo),
        updateSettingsUseCase = UpdateSettingsUseCase(settingsRepo, timeProvider),
        observeNotificationSettingsUseCase = ObserveNotificationSettingsUseCase(notificationRepo),
        updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(notificationRepo, timeProvider),
        exportPdfUseCase = exportPdf,
        exportExcelUseCase = exportExcel,
        exportCsvUseCase = exportCsv,
        printReportUseCase = PrintReportUseCase(saleRepo, printRepo),
        createBackupUseCase = CreateBackupUseCase(saleRepo, inventoryRepo, categoryRepo, settingsRepo, notificationRepo, backupRepo),
        authViewModel = AuthViewModel(
            SignInWithGoogleUseCase(authRepo),
            SignOutUseCase(authRepo),
            GetAuthStateUseCase(authRepo),
            googleNavigator
        ),
        getAuthStateUseCase = GetAuthStateUseCase(authRepo),
        signOutUseCase = SignOutUseCase(authRepo),
        updateDisplayNameUseCase = UpdateDisplayNameUseCase(authRepo),
    )
}
