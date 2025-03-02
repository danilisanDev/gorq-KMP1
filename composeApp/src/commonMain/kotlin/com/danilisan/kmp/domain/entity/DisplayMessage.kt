package com.danilisan.kmp.domain.entity

import org.jetbrains.compose.resources.Resource

class DisplayMessage(
    val res: Resource? = null,
    val arg: String = ""
)
//Extension function in ui.view.gameState.UIDisplay.kt which formats the message to String