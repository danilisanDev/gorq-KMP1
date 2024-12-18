package com.danilisan.kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.danilisan.kmp.core.di.initKoin
import com.danilisan.kmp.core.log.initNapier

fun main(){
    //Start koin DI
    initKoin()

    //Start Napier debug log
    initNapier()

    //Start app in desktop
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KotlinProject",
        ) {
            App()
        }
    }
}