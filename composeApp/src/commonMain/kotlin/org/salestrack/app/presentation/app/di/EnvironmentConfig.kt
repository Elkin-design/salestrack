package org.salestrack.app.presentation.app.di

enum class BackendProvider {
    MOCK,
    FIRESTORE,
}

data class EnvironmentConfig(
    val backendProvider: BackendProvider = BackendProvider.MOCK,
)
