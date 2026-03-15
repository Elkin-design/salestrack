package com.salestrack.domain.repository

import com.salestrack.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String, businessId: String): Result<User>
    suspend fun logout()
    suspend fun recoverPassword(email: String): Result<Unit>
}
