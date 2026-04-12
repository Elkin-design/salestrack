package org.salestrack.app.presentation.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.salestrack.app.domain.model.DashboardSummary

@Preview
@Composable
private fun DashboardLoadingPreview() {
    val snackbarHost = remember { androidx.compose.material3.SnackbarHostState() }
    DashboardScreen(
        uiState = DashboardUiState(isLoading = true),
        snackbarHostState = snackbarHost,
        onRefresh = {},
        onNavigateToReports = {},
        onNavigateToExport = {},
    )
}

@Preview
@Composable
private fun DashboardErrorPreview() {
    val snackbarHost = remember { androidx.compose.material3.SnackbarHostState() }
    DashboardScreen(
        uiState = DashboardUiState(
            isLoading = false,
            errorMessage = "No fue posible cargar el dashboard",
        ),
        snackbarHostState = snackbarHost,
        onRefresh = {},
        onNavigateToReports = {},
        onNavigateToExport = {},
    )
}

@Preview
@Composable
private fun DashboardEmptyPreview() {
    val snackbarHost = remember { androidx.compose.material3.SnackbarHostState() }
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
    )
}
