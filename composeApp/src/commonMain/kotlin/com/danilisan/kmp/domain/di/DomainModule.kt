package com.danilisan.kmp.domain.di

import com.danilisan.kmp.domain.action.gamestate.*
import com.danilisan.kmp.domain.usecase.gamestate.*
import com.danilisan.kmp.domain.usecase.*
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module{
    //Common Usecases
    factoryOf(::GetCurrentDateTimeUseCase)
    factoryOf(::GetFromRepositoryUseCase)

    //Gamestate actions
    singleOf(::GameStateActionManager)
    factoryOf(::LoadGameStateFromModelAction)
    factoryOf(::PressReloadButtonAction)
    factoryOf(::ReloadQueueAction)
    factoryOf(::ReloadRandomBoardAction)
    factoryOf(::NewGameAction)
    factoryOf(::RestartGameAction)
    factoryOf(::BingoAction)
    factoryOf(::CheckBoardStateAction)
    factoryOf(::SelectBoxAction)
    factoryOf(::CompleteSelectionAction)
    factoryOf(::StartLineAction)
    factoryOf(::DragLineAction)
    factoryOf(::EndLineAction)

    //Gamestate Usecases
    factoryOf(::GetSavedGameStateUseCase)
    factoryOf(::SaveGameStateUseCase)
    factoryOf(::GetNumberPoolUseCase)
    factoryOf(::ExcludeFromPoolUseCase)
    factoryOf(::CountStarsOnBoardUseCase)
    factoryOf(::GetWinningLinesUseCase)
    factoryOf(::IsBingoPossibleUseCase)
    factoryOf(::IsSelectionPossibleUseCase)
    factoryOf(::CalculateBoardStateUseCase)
    factoryOf(::CreateEmptyBoardUseCase)
    factoryOf(::CreateParityOrderListUseCase)
    factoryOf(::CreateRandomBoxForBoardUseCase)
    factoryOf(::AddBoxOnBoardUseCase)
    factoryOf(::CreateRandomBoxForQueueUseCase)
    factoryOf(::AddBoxOnQueueUseCase)
    factoryOf(::CheckSelectionResultUseCase)
    factoryOf(::IncrementScoreUseCase)
    factoryOf(::GetDisplayMessageUseCase)
    factoryOf(::UpdateStarBoxOnQueueUseCase)



}