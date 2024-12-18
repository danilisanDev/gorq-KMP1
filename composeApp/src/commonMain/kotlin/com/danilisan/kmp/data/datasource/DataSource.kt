package com.danilisan.kmp.data.datasource

interface DataSource <T> {
    fun loadData(): T?
    fun saveData(data: T)
}