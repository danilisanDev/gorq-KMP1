package com.danilisan.kmp.core.di

import com.danilisan.kmp.data.di.dataModule
import com.danilisan.kmp.domain.di.domainModule
import com.danilisan.kmp.ui.di.uiModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null){
    startKoin{
        config?.invoke(this)
        modules(dataModule, domainModule, uiModule)
    }
}
    //Qualifiers
//    enum class Qualifier{
//        nombre1
//        nombre2
//    }
//
//    single(named(Qualifier.nombre1)) {}
