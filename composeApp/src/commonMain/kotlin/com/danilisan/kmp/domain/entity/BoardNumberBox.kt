package com.danilisan.kmp.domain.entity

/**
 * Represents a numbered tile on the GameBoard.
 * Each instance is identified by its position.
 */
data class BoardNumberBox(
    val number: NumberBox,  //Value: Int and NumberType (REGULAR, BLOCK, STAR)
    val position: BoardPosition, //Column: Int and Row: Int
    val isSelected: Int = -1,//Order of selection (default: not selected)
){
    override fun equals(other: Any?): Boolean {
        if (this === other){
            return true
        }
        if (other !is BoardNumberBox)
            return false

        return position == other.position
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}







