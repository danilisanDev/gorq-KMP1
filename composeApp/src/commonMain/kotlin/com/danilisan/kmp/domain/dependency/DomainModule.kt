package com.danilisan.kmp.domain.dependency

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
    factoryOf(::CheckBoardStateAction)
    factoryOf(::SelectBoxAction)
    factoryOf(::LineStartAction)
    factoryOf(::LineDragAction)
    factoryOf(::LineEndAction)

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
    factoryOf(::UpdateSilverStarValuesUseCase)
    factoryOf(::UpdateGameAction)
    factoryOf(::GetPoolAndParityUseCase)
    factoryOf(::UpdateBoardValuesUseCase)
    factoryOf(::UpdateQueueValuesUseCase)
    factoryOf(::UpdateQueueToBoardValuesUseCase)
}