package org.salestrack.app.presentation.app.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun startAppKoinIfNeeded(config: EnvironmentConfig = EnvironmentConfig()) {
    if (GlobalContext.getOrNull() != null) return

    startKoin {
        modules(appModule(config))
    }
}
