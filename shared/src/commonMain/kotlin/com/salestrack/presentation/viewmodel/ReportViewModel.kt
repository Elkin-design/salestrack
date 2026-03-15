package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.ReportData
import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.SalesRepository
import com.salestrack.domain.util.ReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val salesRepository: SalesRepository,
    private val reportGenerator: ReportGenerator
) : BaseViewModel() {
    private val _dailyReportState = MutableStateFlow<ReportData?>(null)
    val dailyReportState: StateFlow<ReportData?> = _dailyReportState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportState: StateFlow<ExportStatus> = _exportState.asStateFlow()

    sealed class ExportStatus {
        object Idle : ExportStatus()
        object Loading : ExportStatus()
        data class Success(val fileName: String, val data: ByteArray) : ExportStatus()
        data class Error(val message: String) : ExportStatus()
    }

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

    fun exportToPdf(title: String, sales: List<Sale>) {
        val currentReport = _dailyReportState.value ?: return
        viewModelScope.launch {
            _exportState.value = ExportStatus.Loading
            try {
                val data = reportGenerator.generatePdf(title, currentReport, sales)
                _exportState.value = ExportStatus.Success("${title.replace(" ", "_")}.pdf", data)
            } catch (e: Exception) {
                _exportState.value = ExportStatus.Error(e.message ?: "Failed to generate PDF")
            }
        }
    }

    fun exportToExcel(title: String, sales: List<Sale>) {
        viewModelScope.launch {
            _exportState.value = ExportStatus.Loading
            try {
                val data = reportGenerator.generateExcel(title, sales)
                _exportState.value = ExportStatus.Success("${title.replace(" ", "_")}.xlsx", data)
            } catch (e: Exception) {
                _exportState.value = ExportStatus.Error(e.message ?: "Failed to generate Excel")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportStatus.Idle
    }
}
