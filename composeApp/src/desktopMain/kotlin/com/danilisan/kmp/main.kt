package com.danilisan.kmp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.danilisan.kmp.core.dependency.initKoin
import com.danilisan.kmp.core.log.initNapier
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.icon_desktop
import org.jetbrains.compose.resources.painterResource

fun main(){
    //Start koin DI
    initKoin()

    //Start Napier debug log
    initNapier()

    //Start app in desktop
    application {
        val state = rememberWindowState(
            width = 400.dp,
            height = 700.dp,
        )
        val icon = painterResource(Res.drawable.icon_desktop)
        Window(
            onCloseRequest = ::exitApplication,
            title = "gorq",
            state = state,
            icon = icon,
            resizable = false,
        ) {
            App()
        }
    }
}