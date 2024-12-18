package com.danilisan.kmp.data.datasource.local

import com.danilisan.kmp.data.datasource.GameStateDataSource
import com.danilisan.kmp.data.model.GameStateModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameStateSettingsDataSource(
    override val settings: Settings
): SettingsDataSource<GameStateModel>, GameStateDataSource {
    companion object{
        const val KEY_GAME_STATE = "game_state"
    }

   override fun loadData(): GameStateModel? {
       val jsonString = settings[KEY_GAME_STATE, ""]
       return if(jsonString.isNotEmpty()){
           Json.decodeFromString(jsonString)
       }else{
           null
       }
    }

    override fun saveData(data: GameStateModel) {
        val jsonString = Json.encodeToString(data)
        settings.putString(KEY_GAME_STATE, jsonString)
    }
}