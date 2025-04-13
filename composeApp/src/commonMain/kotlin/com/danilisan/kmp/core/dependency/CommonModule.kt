package com.danilisan.kmp.core.dependency

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.core.provider.ProductionDispatcherProvider

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
    singleOf(::ProductionDispatcherProvider){
        bind<DispatcherProvider>()
    }
}