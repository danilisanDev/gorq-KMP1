package com.danilisan.kmp.data.datasource.local

import com.danilisan.kmp.data.datasource.DataSource
import com.russhwolf.settings.Settings

interface SettingsDataSource<T>: DataSource<T> {
    val settings: Settings
}