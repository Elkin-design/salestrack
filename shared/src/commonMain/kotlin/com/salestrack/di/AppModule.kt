package com.salestrack.di

import com.salestrack.data.local.SqlDelightProductRepository
import com.salestrack.data.local.SqlDelightSalesRepository
import com.salestrack.data.remote.FirebaseAuthRepository
import com.salestrack.data.remote.FirebaseSalesDataSource
import com.salestrack.data.sync.SyncManager
import com.salestrack.domain.repository.AuthRepository
import com.salestrack.domain.repository.ProductRepository
import com.salestrack.domain.repository.SalesRepository
import com.salestrack.presentation.viewmodel.SalesViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    single { FirebaseSalesDataSource() }
    single<AuthRepository> { FirebaseAuthRepository() }
    single<ProductRepository> { SqlDelightProductRepository(get()) }
    single<SalesRepository> { SqlDelightSalesRepository(get()) }
    single { SyncManager(get(), get(), "default_biz") }
    
    factory { SalesViewModel(get()) }
    factory { AuthViewModel(get()) }
    factory { ProductViewModel(get()) }
}

expect fun platformModule(): Module
