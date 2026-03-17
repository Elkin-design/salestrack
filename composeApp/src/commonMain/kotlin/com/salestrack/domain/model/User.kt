package com.salestrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val businessId: String
)

enum class UserRole {
    ADMIN, SUPERVISOR, VENDOR
}
