package org.salestrack.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String,
    val productName: String,
    val category: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double,
    val createdAtMillis: Long,
    val sellerName: String,
    val isDeleted: Boolean = false,
) {
    val grossTotal: Double = quantity * unitPrice
    val netTotal: Double = (grossTotal - discount).coerceAtLeast(0.0)
}

data class NewSaleInput(
    val productName: String,
    val category: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double,
    val sellerName: String,
)

data class DashboardSummary(
    val totalSoldToday: Double,
    val transactionCountToday: Int,
    val topProductToday: String,
    val syncStatus: String,
)

