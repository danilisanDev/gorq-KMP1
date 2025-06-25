package com.danilisan.kmp.domain.entity

import androidx.compose.runtime.Stable

/**
 * Represents a box on the game (on Board or Queue),
 * with a type and its numbered value.
 */
sealed interface NumberBox {
    val value: Int
    fun setValue(newValue: Int) {
       //Overriden only when value is mutable (var). Do nothing by default.
    }

    @Stable
    data class RegularBox(
        override val value: Int,
    ) : NumberBox{
        override fun equals(other: Any?): Boolean = (this === other)
        override fun hashCode(): Int = super.hashCode()
        override fun toString(): String = "[${this::class.simpleName}]: $value"
    }

    @Stable
    data class BlockBox(
        override val value: Int,
    ) : NumberBox{
        override fun equals(other: Any?): Boolean = (this === other)
        override fun hashCode(): Int = super.hashCode()
        override fun toString(): String = "[${this::class.simpleName}]: $value"
    }

    @Stable
    class EmptyBox: NumberBox {
        override val value = EMPTY_VALUE
    }

    sealed interface StarBox : NumberBox

    @Stable
    class GoldenStarBox : StarBox{
        override val value = EMPTY_VALUE
        override fun toString(): String = "[GoldenStar]"
    }

    @Stable
    class SilverStarBox: StarBox {
        override var value: Int = EMPTY_VALUE
            private set

        override fun setValue(newValue: Int) {
            value = newValue
        }
        override fun toString(): String = "[SilverStar]"
    }

    companion object {
        const val EMPTY_VALUE = -10
    }
}

