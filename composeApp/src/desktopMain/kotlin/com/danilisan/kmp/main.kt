package com.danilisan.kmp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.danilisan.kmp.core.di.initKoin
import com.danilisan.kmp.core.log.initNapier
import java.awt.Toolkit

fun main(){
    //Start koin DI
    initKoin()

    //Start Napier debug log
    initNapier()

    //Start app in desktop
    application {
        val state = rememberWindowState(
            width = 400.dp,
            height = 800.dp,
        )
        Window(
            onCloseRequest = ::exitApplication,
            title = "KotlinProject",
            state = state
        ) {
            App()
        }
    }
}