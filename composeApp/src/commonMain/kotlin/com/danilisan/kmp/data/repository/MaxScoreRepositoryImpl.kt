package com.danilisan.kmp.data.repository

import com.danilisan.kmp.data.datasource.MaxScoreDataSource
import com.danilisan.kmp.data.datasource.local.MaxScoreSettingsDataSource
import com.danilisan.kmp.data.model.MaxScoreModel
import com.danilisan.kmp.domain.repository.MaxScoreRepository

class MaxScoreRepositoryImpl (
    dataSource: MaxScoreSettingsDataSource,
): MaxScoreRepository {
    override val dataSources: List<MaxScoreDataSource> = listOf(dataSource)

    override suspend fun getElement(gameModeId: Int): MaxScoreModel? {
        return dataSources.first().loadData(gameModeId)
    }

    override suspend fun updateElement(element: MaxScoreModel, gameModeId: Int) {
        dataSources.first().saveData(element, gameModeId)
    }
}
