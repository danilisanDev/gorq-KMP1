package com.danilisan.kmp.domain.entity

class Board(
    var numberBoxes: MutableList<BoardNumberBox> = mutableListOf()
) {

    fun newRandomBoard() {
        this.numberBoxes = mutableListOf()
    }

    fun sumAllNumbers(): Int {
        return 0
    }

    fun isBlocked(): Boolean {
        return false
    }

}