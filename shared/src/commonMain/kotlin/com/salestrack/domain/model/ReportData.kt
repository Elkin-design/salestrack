package com.salestrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportData(
    val totalSales: Double,
    val count: Int,
    val categoryBreakdown: Map<String, Double>,
    val bestSellingProduct: String?,
    val timestamp: Long
)
