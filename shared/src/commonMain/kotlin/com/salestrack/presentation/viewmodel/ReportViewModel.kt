package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.ReportData
import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val salesRepository: SalesRepository) : BaseViewModel() {
    private val _dailyReportState = MutableStateFlow<ReportData?>(null)
    val dailyReportState: StateFlow<ReportData?> = _dailyReportState.asStateFlow()

    fun generateDailyReport(sales: List<Sale>) {
        viewModelScope.launch {
            if (sales.isEmpty()) {
                _dailyReportState.value = null
                return@launch
            }

            val total = sales.sumOf { it.totalAmount }
            val count = sales.size
            val breakdown = sales.groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            val bestSelling = sales.groupBy { it.productName }
                .maxByOrNull { it.value.size }?.key

            _dailyReportState.value = ReportData(
                totalSales = total,
                count = count,
                categoryBreakdown = breakdown,
                bestSellingProduct = bestSelling,
                timestamp = if (sales.isNotEmpty()) sales.first().timestamp else 0L
            )
        }
    }
}
