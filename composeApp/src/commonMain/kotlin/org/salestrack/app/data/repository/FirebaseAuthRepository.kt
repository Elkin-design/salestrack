package org.salestrack.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.AuthRepository
import org.salestrack.app.domain.repository.AuthUser

class FirebaseAuthRepository : AuthRepository {

    private val auth by lazy { Firebase.auth }

    override fun observeAuthState(): Flow<AuthUser?> {
        return auth.authStateChanged.map { user ->
            user?.toDomain()
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val authResult = auth.signInWithCredential(credential)
            val user = authResult.user?.toDomain()
            if (user != null) {
                AppResult.Success(user)
            } else {
                AppResult.Failure(IllegalStateException("Error al obtener datos del usuario"))
            }
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        return try {
            auth.signOut()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override fun getCurrentUser(): AuthUser? {
        return auth.currentUser?.toDomain()
    }

    override suspend fun updateDisplayName(name: String): AppResult<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.updateProfile(displayName = name)
                AppResult.Success(Unit)
            } else {
                AppResult.Failure(IllegalStateException("No user is signed in"))
            }
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoURL
        )
    }
}
