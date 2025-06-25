package com.danilisan.kmp.data.datasource.local

import com.russhwolf.settings.Settings

interface SettingsDataSource<T> {
    val settings: Settings
}