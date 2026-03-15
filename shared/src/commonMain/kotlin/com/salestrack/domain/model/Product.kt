package com.salestrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val unitOfMeasure: String,
    val barcode: String? = null,
    val categoryId: String,
    val stock: Int,
    val minStockThreshold: Int
)
