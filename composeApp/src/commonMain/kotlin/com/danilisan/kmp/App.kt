package com.danilisan.kmp

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.danilisan.kmp.ui.view.GameScreen



@Composable
fun App() {
    MaterialTheme {
            Navigator(screen = GameScreen())
    }
}