package com.danilisan.kmp.domain.entity

data class Line (
    val direction: LineDirection,
    val originPos: Int,
){
    companion object{
        enum class LineDirection {
            HORIZONTAL,
            VERTICAL,
            DIAGONAL
        }
    }
}