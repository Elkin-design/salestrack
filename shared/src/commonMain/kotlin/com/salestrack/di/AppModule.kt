package com.salestrack.di

import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    // Repositories and Use Cases will be added here
}

expect fun platformModule(): Module
