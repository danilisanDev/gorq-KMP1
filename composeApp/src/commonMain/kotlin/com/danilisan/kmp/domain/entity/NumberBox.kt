package com.danilisan.kmp.domain.entity

sealed interface NumberBox {
    val value: Int
    fun setValue(newValue: Int) {}

    data class RegularBox(override val value: Int): NumberBox
    data class BlockBox(override val value: Int): NumberBox
    class StarBox: NumberBox{
        override var value: Int = 0
            private set
        override fun setValue(newValue: Int) {
            value = newValue
        }
    }
}

