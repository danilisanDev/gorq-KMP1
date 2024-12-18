package com.danilisan.kmp.domain.usecase

import com.danilisan.kmp.domain.usecase.gamestate.GetGameStateModelUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateNumberPoolUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GenerateQueueUseCase

/**
 * Usecase manager for GameStateViewModel
 */
class GameStateUseCaseManager (
    val getGameStateModel: GetGameStateModelUseCase,
    val generateBoard: GenerateBoardUseCase,
    val generateQueue: GenerateQueueUseCase,
    val getNumberPoolUseCase: GenerateNumberPoolUseCase,
){
}