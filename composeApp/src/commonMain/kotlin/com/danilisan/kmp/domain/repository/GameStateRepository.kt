package com.danilisan.kmp.domain.repository

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.model.GameStateModel

interface GameStateRepository{
    val dataSources: List<GameStateDataSource>
    suspend fun getElement(): GameStateModel?
    suspend fun updateElement(element: GameStateModel)
}