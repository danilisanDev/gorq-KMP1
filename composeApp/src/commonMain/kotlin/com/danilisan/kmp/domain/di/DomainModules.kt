package com.danilisan.kmp.domain.di

import com.danilisan.kmp.domain.usecase.GameStateUseCaseManager
import com.danilisan.kmp.domain.usecase.GetCurrentDateTimeUseCase
import com.danilisan.kmp.domain.usecase.GetFromRepositoryUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetGameStateModelUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateNumberPoolUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateQueueUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module{
    //Managers
    factoryOf(::GameStateUseCaseManager)

    //Common Usecases
    factoryOf(::GetCurrentDateTimeUseCase)
    factoryOf(::GetFromRepositoryUseCase)

    //Gamestate Usecases
    factoryOf(::GetGameStateModelUseCase)
    factoryOf(::GenerateBoardUseCase)
    factoryOf(::GenerateQueueUseCase)
    factoryOf(::GenerateNumberPoolUseCase)

}