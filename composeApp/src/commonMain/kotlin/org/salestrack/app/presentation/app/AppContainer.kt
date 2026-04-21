package org.salestrack.app.presentation.app

import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.utils.TimeProvider
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
import org.salestrack.app.domain.usecase.settings.PopulateSampleDataUseCase
import org.salestrack.app.domain.usecase.settings.UpdateSettingsUseCase
import org.salestrack.app.domain.usecase.team.GetRolePermissionsUseCase
import org.salestrack.app.domain.usecase.team.GetTeamSalesUseCase
import org.salestrack.app.domain.usecase.team.InviteMemberUseCase
import org.salestrack.app.presentation.feature.auth.AuthViewModel
import org.salestrack.app.domain.usecase.auth.GetAuthStateUseCase
import org.salestrack.app.domain.usecase.auth.SignOutUseCase

class AppContainer(
    val dispatcherProvider: DispatcherProvider,
    val timeProvider: TimeProvider,
    val saleRepository: SaleRepository,
    val inventoryRepository: InventoryRepository,
    val categoryRepository: CategoryRepository,
    val settingsRepository: SettingsRepository,
    val notificationRepository: NotificationRepository,
    val exportRepository: ExportRepository,
    val printRepository: PrintRepository,
    val backupRepository: BackupRepository,
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
    val deleteProductUseCase: DeleteProductUseCase,
    val filterProductsUseCase: FilterProductsUseCase,
    val adjustStockUseCase: AdjustStockUseCase,
    val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    val importCatalogCsvUseCase: ImportCatalogCsvUseCase,
    val exportCatalogCsvUseCase: ExportCatalogCsvUseCase,
    val exportCatalogExcelUseCase: ExportCatalogExcelUseCase,
    val observeCategoriesUseCase: ObserveCategoriesUseCase,
    val createCategoryUseCase: CreateCategoryUseCase,
    val updateCategoryUseCase: UpdateCategoryUseCase,
    val deleteCategoryUseCase: DeleteCategoryUseCase,
    val observeSettingsUseCase: ObserveSettingsUseCase,
    val updateSettingsUseCase: UpdateSettingsUseCase,
    val populateSampleDataUseCase: PopulateSampleDataUseCase,
    val observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase,
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    val exportPdfUseCase: ExportPdfUseCase,
    val exportExcelUseCase: ExportExcelUseCase,
    val exportCsvUseCase: ExportCsvUseCase,
    val printReportUseCase: PrintReportUseCase,
    val createBackupUseCase: CreateBackupUseCase,
    val authViewModel: AuthViewModel,
    val getAuthStateUseCase: GetAuthStateUseCase,
    val signOutUseCase: SignOutUseCase,
)

