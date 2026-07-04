package org.salestrack.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import org.salestrack.app.domain.repository.RemoteConfigRepository

class FirebaseRemoteConfigRepository : RemoteConfigRepository {

    private val remoteConfig by lazy { Firebase.remoteConfig }

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate()
        } catch (e: Exception) {
            false
        }
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getValue(key).asBoolean()
    }

    override fun getString(key: String): String {
        return remoteConfig.getValue(key).asString()
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getValue(key).asLong()
    }

    override fun getDouble(key: String): Double {
        return remoteConfig.getValue(key).asDouble()
    }
}
