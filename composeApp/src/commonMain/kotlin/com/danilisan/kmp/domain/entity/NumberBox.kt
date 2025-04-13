package com.danilisan.kmp.domain.entity


sealed interface NumberBox {
    val value: Int
    fun setValue(newValue: Int) {}

    data class RegularBox(
        override val value: Int,
    ) : NumberBox{
        override fun equals(other: Any?): Boolean = (this === other)
        override fun hashCode(): Int = super.hashCode()
        override fun toString(): String = "[${this::class.simpleName}]: $value"
    }

    data class BlockBox(
        override val value: Int,
    ) : NumberBox{
        override fun equals(other: Any?): Boolean = (this === other)
        override fun hashCode(): Int = super.hashCode()
        override fun toString(): String = "[${this::class.simpleName}]: $value"
    }

    class EmptyBox: NumberBox {
        override val value = EMPTY_VALUE
    }

    sealed interface StarBox : NumberBox

    class GoldenStarBox : StarBox{
        override val value = EMPTY_VALUE
    }

    class SilverStarBox: StarBox {
        override var value: Int = EMPTY_VALUE
            private set

        override fun setValue(newValue: Int) {
            value = newValue
        }
    }

    companion object {
        const val EMPTY_VALUE = -10
    }
}

