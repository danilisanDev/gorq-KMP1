package com.danilisan.kmp.data.repository

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.datasource.local.GameStateSettingsDataSource
import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.domain.repository.GameStateRepository

class GameStateRepositoryImpl(
    dataSource: GameStateSettingsDataSource
): GameStateRepository{
    override val dataSources: List<GameStateDataSource> = listOf(dataSource)

    override fun getElement(): GameStateModel? {
        val model: GameStateModel? = dataSources.first().loadData()
        return model
    }

    override fun updateElement(element: GameStateModel) {
        dataSources.first().saveData(element)
    }
}