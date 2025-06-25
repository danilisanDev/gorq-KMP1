package com.danilisan.kmp.data.repository

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.datasource.local.GameStateSettingsDataSource
import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.domain.repository.GameStateRepository

class GameStateRepositoryImpl(
    dataSource: GameStateSettingsDataSource,
): GameStateRepository{
    override val dataSources: List<GameStateDataSource> = listOf(dataSource)

    override suspend fun getElement(): GameStateModel? {
        return dataSources.first().loadData()
    }

    override suspend fun updateElement(element: GameStateModel) {
        dataSources.first().saveData(element)
    }
}