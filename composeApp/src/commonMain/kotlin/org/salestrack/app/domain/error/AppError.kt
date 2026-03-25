package org.salestrack.app.domain.error

/**
 * Errores de dominio tipados para mantener reglas explícitas.
 */
sealed interface AppError {
    data class Validation(val message: String) : AppError
    data class NotFound(val message: String) : AppError
    data class Forbidden(val message: String) : AppError
    data class Conflict(val message: String) : AppError
    data class Unexpected(val message: String, val cause: Throwable? = null) : AppError
}

