package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun UIAdBanner(text: String){
    Text(
        text = text,
        fontSize = 20.sp,
    )
}