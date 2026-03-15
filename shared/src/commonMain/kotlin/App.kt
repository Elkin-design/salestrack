package com.salestrack.presentation

import androidx.compose.runtime.*
import com.salestrack.presentation.theme.SalesTrackTheme
import com.salestrack.presentation.ui.auth.LoginScreen
import com.salestrack.presentation.ui.dashboard.DashboardScreen
import com.salestrack.presentation.viewmodel.AuthViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        SalesTrackTheme {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
            val authViewModel: AuthViewModel = koinInject()
            val salesViewModel: com.salestrack.presentation.viewmodel.SalesViewModel = koinInject()
            val productViewModel: com.salestrack.presentation.viewmodel.ProductViewModel = koinInject()
            val reportViewModel: com.salestrack.presentation.viewmodel.ReportViewModel = koinInject()

            when (currentScreen) {
                Screen.Login -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { currentScreen = Screen.Dashboard }
                )
                Screen.Dashboard -> DashboardScreen(
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = Screen.Login
                    },
                    onNavigateToRegisterSale = { currentScreen = Screen.RegisterSale },
                    onNavigateToCatalog = { currentScreen = Screen.Catalog },
                    onNavigateToReports = { currentScreen = Screen.Reports }
                )
                Screen.RegisterSale -> com.salestrack.presentation.ui.sales.SalesRegistrationScreen(
                    viewModel = salesViewModel,
                    onBack = { currentScreen = Screen.Dashboard }
                )
                Screen.Catalog -> com.salestrack.presentation.ui.catalog.CatalogScreen(
                    viewModel = productViewModel,
                    onBack = { currentScreen = Screen.Dashboard },
                    onNavigateToAddProduct = { currentScreen = Screen.AddProduct }
                )
                Screen.AddProduct -> com.salestrack.presentation.ui.catalog.AddProductScreen(
                    viewModel = productViewModel,
                    onBack = { currentScreen = Screen.Catalog }
                )
                Screen.Reports -> com.salestrack.presentation.ui.reports.ReportsScreen(
                    reportViewModel = reportViewModel,
                    salesViewModel = salesViewModel,
                    onBack = { currentScreen = Screen.Dashboard }
                )
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object RegisterSale : Screen()
    object Catalog : Screen()
    object AddProduct : Screen()
    object Reports : Screen()
}