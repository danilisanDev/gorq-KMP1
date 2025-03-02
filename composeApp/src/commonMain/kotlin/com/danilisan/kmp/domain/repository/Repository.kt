package com.danilisan.kmp.domain.repository

import com.danilisan.kmp.data.datasource.DataSource

interface Repository<T> {
    val dataSources: List<DataSource<T>>
    suspend fun getElement(): T?
    suspend fun updateElement(element: T)
}