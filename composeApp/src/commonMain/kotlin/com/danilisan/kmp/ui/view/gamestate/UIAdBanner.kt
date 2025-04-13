package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.danilisan.kmp.ui.theme.Theme

@Composable
fun UIAdBanner(text: String){
    Box(
        Modifier
            .fillMaxSize()
            .background(
                color = Theme.colors.primary
            ),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = text,
            fontSize = 20.sp,
        )
    }

}