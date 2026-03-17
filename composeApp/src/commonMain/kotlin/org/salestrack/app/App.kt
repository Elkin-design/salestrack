package org.salestrack.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.salestrack.domain.model.UserRole
import com.salestrack.presentation.viewmodel.AuthState
import com.salestrack.presentation.viewmodel.AuthViewModel
import com.salestrack.presentation.viewmodel.ProductViewModel
import com.salestrack.presentation.viewmodel.ReportViewModel
import com.salestrack.presentation.viewmodel.SalesViewModel
import com.salestrack.presentation.ui.auth.LoginScreen
import com.salestrack.presentation.ui.dashboard.DashboardScreen
import com.salestrack.presentation.ui.catalog.CatalogScreen
import com.salestrack.presentation.ui.sales.SalesRegistrationScreen
import com.salestrack.presentation.ui.reports.ReportsScreen
import com.salestrack.util.BarcodeScanner
import org.koin.compose.koinInject

private val PrimaryBlue = Color(0xFF1E88E5)
private val IndigoPurple = Color(0xFF5E35B1)
private val AccentTeal = Color(0xFF00BCD4)

enum class Screen { Login, Dashboard, Catalog, Sales, Reports }

@Composable
fun App() {
    val colorScheme = lightColorScheme(
        primary = PrimaryBlue,
        secondary = IndigoPurple,
        tertiary = AccentTeal
    )

    MaterialTheme(colorScheme = colorScheme) {
        val authViewModel: AuthViewModel = koinInject()
        val salesViewModel: SalesViewModel = koinInject()
        val productViewModel: ProductViewModel = koinInject()
        val reportViewModel: ReportViewModel = koinInject()
        val barcodeScanner: BarcodeScanner = koinInject()

        val authState by authViewModel.authState.collectAsState()
        var currentScreen by remember { mutableStateOf(Screen.Login) }
        val userRole = if (authState is AuthState.Authenticated) UserRole.VENDOR else UserRole.VENDOR

        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated) currentScreen = Screen.Dashboard
            else if (authState is AuthState.Idle) currentScreen = Screen.Login
        }

        when (currentScreen) {
            Screen.Login -> LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { currentScreen = Screen.Dashboard }
            )
            Screen.Dashboard -> DashboardScreen(
                userRole = userRole,
                onLogout = { authViewModel.logout(); currentScreen = Screen.Login },
                onNavigateToRegisterSale = { currentScreen = Screen.Sales },
                onNavigateToCatalog = { currentScreen = Screen.Catalog },
                onNavigateToReports = { currentScreen = Screen.Reports }
            )
            Screen.Catalog -> CatalogScreen(
                userRole = userRole,
                viewModel = productViewModel,
                onBack = { currentScreen = Screen.Dashboard },
                onNavigateToAddProduct = { currentScreen = Screen.Catalog }
            )
            Screen.Sales -> SalesRegistrationScreen(
                viewModel = salesViewModel,
                barcodeScanner = barcodeScanner,
                onBack = { currentScreen = Screen.Dashboard }
            )
            Screen.Reports -> ReportsScreen(
                reportViewModel = reportViewModel,
                salesViewModel = salesViewModel,
                onBack = { currentScreen = Screen.Dashboard }
            )
        }
    }
}
