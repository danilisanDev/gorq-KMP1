package com.danilisan.kmp.data.di

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.datasource.local.GameStateSettingsDataSource
import com.danilisan.kmp.data.datasource.local.SettingsDataSource
import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.data.repository.GameStateRepositoryImpl
import com.danilisan.kmp.domain.repository.GameStateRepository
import com.danilisan.kmp.domain.repository.Repository
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.reflect.KClass

val dataModule = module{
    //Data sources
    single<Settings>{ Settings() }

    factoryOf(::GameStateSettingsDataSource){
        bind<SettingsDataSource<GameStateModel>>()
        bind<GameStateDataSource>()
    }

    //Repositories
    factoryOf(::GameStateRepositoryImpl){
        bind<GameStateRepository>()
    }

    single<Map<KClass<*>, Repository<*>>>{
        mapOf(
            GameStateModel::class to get<Repository<GameStateModel>>()
        )
    }

}