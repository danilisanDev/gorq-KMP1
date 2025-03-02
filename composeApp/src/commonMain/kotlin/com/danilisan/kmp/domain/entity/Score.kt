package com.danilisan.kmp.domain.entity

class Score(
    val points: Long = 0L,
    val lines: Int = 0,
    val turns: Int = 0,
) {
    operator fun plus(other: Score?) = when{
        other == null -> this
        other.points < 0 -> Score()
        else -> Score(
            this.points + other.points,
            this.lines + other.lines,
            this.turns + other.turns,
        )
    }
}