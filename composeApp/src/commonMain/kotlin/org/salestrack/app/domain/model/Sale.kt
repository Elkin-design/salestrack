package org.salestrack.app.domain.model

import kotlinx.serialization.Serializable

enum class PaymentMethod {
    CASH,
    CARD,
    DIGITAL_WALLET
}

@Serializable
data class SaleItem(
    val productId: String,
    val productName: String,
    val category: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
) {
    val grossTotal: Double get() = quantity * unitPrice
    val netTotal: Double get() = (grossTotal - discount).coerceAtLeast(0.0)
}

@Serializable
data class Sale(
    val id: String,
    val createdAtMillis: Long,
    val sellerName: String,
    val isDeleted: Boolean = false,
    
    // Nuevos campos para POS
    val items: List<SaleItem> = emptyList(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val globalDiscount: Double = 0.0,
    
    // Campos antiguos (mantenidos con valores por defecto para retrocompatibilidad con Firestore)
    val productName: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val discount: Double = 0.0,
    val productId: String? = null,
) {
    val grossTotal: Double 
        get() = if (items.isNotEmpty()) {
            items.sumOf { it.grossTotal }
        } else {
            quantity * unitPrice
        }
        
    val netTotal: Double 
        get() = if (items.isNotEmpty()) {
            (items.sumOf { it.netTotal } - globalDiscount).coerceAtLeast(0.0)
        } else {
            (grossTotal - discount).coerceAtLeast(0.0)
        }
}

data class NewSaleInput(
    val items: List<SaleItem>,
    val paymentMethod: PaymentMethod,
    val globalDiscount: Double,
    val sellerName: String,
    val createdAtMillis: Long? = null,
)

data class DashboardSummary(
    val totalSoldToday: Double,
    val transactionCountToday: Int,
    val topProductToday: String,
    val syncStatus: String,
)

