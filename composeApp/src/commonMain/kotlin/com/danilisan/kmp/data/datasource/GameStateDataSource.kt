package com.danilisan.kmp.data.datasource

import com.danilisan.kmp.data.model.GameStateModel

interface GameStateDataSource {
    fun loadData(): GameStateModel?
    fun saveData(data: GameStateModel)
}