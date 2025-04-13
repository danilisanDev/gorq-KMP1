package com.danilisan.kmp.ui.dependency

import androidx.lifecycle.SavedStateHandle
import com.danilisan.kmp.ui.viewmodel.GameStateViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module{
    factory<SavedStateHandle> { SavedStateHandle() }
    viewModelOf(::GameStateViewModel)
}