package com.danilisan.kmp.domain.entity

class Score(
    val points: Long = 0L,
    val lines: Int = 0,
    val turns: Int = 0,
    val increment: Long = 0,
    val maxPoints: Long = 0L
) {
    operator fun plus(other: Score?) = when{
        other == null -> this
        other.points < 0 -> Score()
        else -> Score(
            points = this.points + other.points,
            lines = this.lines + other.lines,
            turns = this.turns + other.turns,
            increment = other.points,
            maxPoints = other.maxPoints,
        )
    }
}