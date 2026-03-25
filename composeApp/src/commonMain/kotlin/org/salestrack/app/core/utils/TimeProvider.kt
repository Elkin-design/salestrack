package org.salestrack.app.core.utils

/**
 * Fuente de tiempo para permitir pruebas deterministas.
 * Interfaz que abstrae la obtención de tiempo del sistema.
 */
interface TimeProvider {
    fun nowMillis(): Long
}

/**
 * Implementación por defecto que obtiene el tiempo del sistema.
 * Esta implementación es compatible con todas las plataformas soportadas.
 */
class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long {
        // En Kotlin multiplatform, podemos usar expect/actual
        // pero por ahora usamos una solución simple compatible
        return getSystemTimeMillis()
    }
}

// Función para obtener el tiempo del sistema de manera multiplataforma
internal expect fun getSystemTimeMillis(): Long
