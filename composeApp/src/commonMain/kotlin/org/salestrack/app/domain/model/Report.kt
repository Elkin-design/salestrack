package org.salestrack.app.domain.model

enum class ReportPeriod {
    Daily,
    Weekly,
    Monthly,
    Annual,
    Custom,
}

data class ReportRange(
    val fromMillis: Long,
    val toMillis: Long,
)

data class ReportSummary(
    val totalAmount: Double,
    val transactionCount: Int,
    val averageTicket: Double,
    val topProductByVolume: String,
    val topProductByValue: String,
    val categoryBreakdown: List<CategoryAmount>,
)

data class CategoryAmount(
    val category: String,
    val amount: Double,
)

data class ReportPoint(
    val label: String,
    val totalAmount: Double,
    val transactionCount: Int,
)

data class ReportData(
    val range: ReportRange,
    val summary: ReportSummary,
    val points: List<ReportPoint>,
    val transactions: List<Sale>,
)

