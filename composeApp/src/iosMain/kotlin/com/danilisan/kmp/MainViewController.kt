package com.danilisan.kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.danilisan.kmp.core.di.initKoin
import com.danilisan.kmp.core.log.initNapier

fun MainViewController(
    configure: () -> Unit = {
        initKoin()
        initNapier()
    }
) = ComposeUIViewController { App() }