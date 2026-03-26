package org.salestrack.app.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.salestrack.app.core.dispatcher.DefaultDispatcherProvider
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.utils.SystemTimeProvider
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.sales.AddSaleUseCase
import org.salestrack.app.domain.usecase.sales.DeleteSaleUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.domain.usecase.sales.UpdateSaleUseCase

class AppContainer(
    val dispatcherProvider: DispatcherProvider,
    val timeProvider: TimeProvider,
    val saleRepository: SaleRepository,
    val addSaleUseCase: AddSaleUseCase,
    val updateSaleUseCase: UpdateSaleUseCase,
    val deleteSaleUseCase: DeleteSaleUseCase,
    val filterSalesUseCase: FilterSalesUseCase,
    val buildDashboardSummaryUseCase: BuildDashboardSummaryUseCase,
)

@Composable
fun rememberAppContainer(): AppContainer {
    return remember {
        val dispatchers = DefaultDispatcherProvider()
        val timeProvider = SystemTimeProvider()
        val dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider))
        val repository = FakeSaleRepository(dataSource = dataSource, timeProvider = timeProvider)

        AppContainer(
            dispatcherProvider = dispatchers,
            timeProvider = timeProvider,
            saleRepository = repository,
            addSaleUseCase = AddSaleUseCase(repository),
            updateSaleUseCase = UpdateSaleUseCase(repository),
            deleteSaleUseCase = DeleteSaleUseCase(repository),
            filterSalesUseCase = FilterSalesUseCase(),
            buildDashboardSummaryUseCase = BuildDashboardSummaryUseCase(),
        )
    }
}

