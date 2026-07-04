package org.salestrack.app.domain.repository

interface RemoteConfigRepository {
    suspend fun fetchAndActivate(): Boolean
    
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    fun getLong(key: String): Long
    fun getDouble(key: String): Double
}
