package com.danilisan.kmp.domain.repository

import com.danilisan.kmp.data.datasource.MaxScoreDataSource
import com.danilisan.kmp.data.model.MaxScoreModel

interface MaxScoreRepository {
    val dataSources: List<MaxScoreDataSource>
    suspend fun getElement(gameModeId: Int): MaxScoreModel?
    suspend fun updateElement(element: MaxScoreModel, gameModeId: Int)
}