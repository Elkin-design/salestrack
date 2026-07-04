package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult

/**
 * User model simplified for auth state.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

interface AuthRepository {
    /**
     * Observes the current authentication state.
     * Returns null if no user is signed in.
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Sign in with a Google ID Token.
     */
    suspend fun signInWithGoogle(idToken: String): AppResult<AuthUser>

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): AppResult<Unit>

    /**
     * Returns the current user immediately if available.
     */
    fun getCurrentUser(): AuthUser?

    /**
     * Update the display name of the current user.
     */
    suspend fun updateDisplayName(name: String): AppResult<Unit>
}
