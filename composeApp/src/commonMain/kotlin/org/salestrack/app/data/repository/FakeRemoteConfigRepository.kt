package org.salestrack.app.data.repository

import org.salestrack.app.domain.repository.RemoteConfigRepository

class FakeRemoteConfigRepository(
    private val defaults: Map<String, Any> = emptyMap()
) : RemoteConfigRepository {

    override suspend fun fetchAndActivate(): Boolean {
        // En una implementación Fake simplemente retornamos true simulando éxito
        return true
    }

    override fun getBoolean(key: String): Boolean {
        return (defaults[key] as? Boolean) ?: false
    }

    override fun getString(key: String): String {
        return (defaults[key] as? String) ?: ""
    }

    override fun getLong(key: String): Long {
        return (defaults[key] as? Long) ?: 0L
    }

    override fun getDouble(key: String): Double {
        return (defaults[key] as? Double) ?: 0.0
    }
}
