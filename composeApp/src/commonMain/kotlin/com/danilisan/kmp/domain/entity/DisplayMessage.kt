package com.danilisan.kmp.domain.entity

import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Resource

class DisplayMessage(
    val res: Resource? = null,
    val arg: String = "",
    val weight: FontWeight = FontWeight.Normal,
    val sizeDiff: Int = 0,
    val bgColor: Int = 0,
    val highlightColor: Int = 1,
    val icon: DrawableResource? = null,
){
    companion object{
        const val DISPLAY_TEXT_PRIMARY = 0
        const val DISPLAY_TEXT_SECONDARY = 1
        const val DISPLAY_TEXT_SELECTED = 2
        const val DISPLAY_TEXT_SUCCESS = 3
        const val DISPLAY_TEXT_ERROR = 4
        const val DISPLAY_TEXT_GOLDEN = 5
    }
}
//Extension function in ui.view.gameState.UIDisplay.kt which formats the message to String