package org.salestrack.app.domain.model

data class Category(
    val id: String,
    val name: String,
    val colorHex: String,
    val isActive: Boolean = true,
    val updatedAtMillis: Long,
)
