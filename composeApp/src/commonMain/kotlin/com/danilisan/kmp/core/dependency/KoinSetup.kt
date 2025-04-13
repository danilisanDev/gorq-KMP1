package com.danilisan.kmp.core.dependency

import com.danilisan.kmp.data.di.dataModule
import com.danilisan.kmp.domain.dependency.domainModule
import com.danilisan.kmp.ui.dependency.uiModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null){
    startKoin{
        config?.invoke(this)
        modules(commonModule, dataModule, domainModule, uiModule)
    }
}
