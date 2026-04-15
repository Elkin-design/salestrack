package org.salestrack.app.presentation.app.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

import org.salestrack.app.core.di.initializeFirebase

fun startAppKoinIfNeeded(config: EnvironmentConfig = EnvironmentConfig()) {
    if (GlobalContext.getOrNull() != null) return

    initializeFirebase()

    startKoin {
        modules(appModule(config))
    }
}
