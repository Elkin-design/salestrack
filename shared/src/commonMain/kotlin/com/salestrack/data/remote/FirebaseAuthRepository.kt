package com.salestrack.data.remote

import com.salestrack.domain.model.User
import com.salestrack.domain.model.UserRole
import com.salestrack.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override val currentUser: Flow<User?> = auth.authStateChanged.map { firebaseUser ->
        firebaseUser?.let {
            // In a real app, we'd fetch the role and businessId from Firestore
            User(it.uid, it.email ?: "", "User", UserRole.VENDOR, "default_biz")
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password)
        val user = result.user?.let { User(it.uid, it.email ?: "", "User", UserRole.VENDOR, "default_biz") }
        if (user != null) Result.success(user) else Result.failure(Exception("Login failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(email: String, password: String, name: String, businessId: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val user = result.user?.let { User(it.uid, it.email ?: "", name, UserRole.VENDOR, businessId) }
        if (user != null) Result.success(user) else Result.failure(Exception("Registration failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
