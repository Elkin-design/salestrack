package com.salestrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: String,
    val name: String,
    val ownerId: String,
    val registrationDate: Long
)
