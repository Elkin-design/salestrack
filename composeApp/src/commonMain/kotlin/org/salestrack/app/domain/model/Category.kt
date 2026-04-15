package org.salestrack.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val colorHex: String,
    val isActive: Boolean = true,
    val updatedAtMillis: Long,
)
