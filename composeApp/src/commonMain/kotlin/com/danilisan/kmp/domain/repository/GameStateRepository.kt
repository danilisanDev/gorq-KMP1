package com.danilisan.kmp.domain.repository

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.model.GameStateModel

interface GameStateRepository: Repository<GameStateModel>{
    override val dataSources: List<GameStateDataSource>
}