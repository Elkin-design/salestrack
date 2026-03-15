package com.salestrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String,
    val productName: String,
    val productId: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val categoryId: String,
    val vendorId: String,
    val platform: String,
    val timestamp: Long,
    val isDeleted: Boolean = false
)
