package com.danilisan.kmp.data.dependency

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.datasource.MaxScoreDataSource
import com.danilisan.kmp.data.datasource.local.GameStateSettingsDataSource
import com.danilisan.kmp.data.datasource.local.MaxScoreSettingsDataSource
import com.danilisan.kmp.data.datasource.local.SettingsDataSource
import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.data.model.MaxScoreModel
import com.danilisan.kmp.data.repository.GameStateRepositoryImpl
import com.danilisan.kmp.data.repository.MaxScoreRepositoryImpl
import com.danilisan.kmp.domain.repository.GameStateRepository
import com.danilisan.kmp.domain.repository.MaxScoreRepository
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module{
    //Data sources
    single<Settings>{ Settings() }

    factoryOf(::GameStateSettingsDataSource){
        bind<SettingsDataSource<GameStateModel>>()
        bind<GameStateDataSource>()
    }
    factoryOf(::MaxScoreSettingsDataSource){
        bind<SettingsDataSource<MaxScoreModel>>()
        bind<MaxScoreDataSource>()
    }

    //Repositories
    factoryOf(::GameStateRepositoryImpl){
        bind<GameStateRepository>()
    }
    factoryOf(::MaxScoreRepositoryImpl){
        bind<MaxScoreRepository>()
    }
}