package com.danilisan.kmp.domain.entity

sealed interface NumberBox {
    val value: Int
    var boxAnimation: Int
    fun setValue(newValue: Int) {}
    fun setAnimation(newAnimation: Int){
        boxAnimation = if(newAnimation !in AVAILABLE_ANIMATIONS){
            NO_ANIMATION
        }else{
            newAnimation
        }
    }

    data class RegularBox(
        override val value: Int,
        override var boxAnimation: Int = NO_ANIMATION
    ) : NumberBox

    data class BlockBox(
        override val value: Int,
        override var boxAnimation: Int = NO_ANIMATION
    ) : NumberBox

    data class EmptyBox(
        override var boxAnimation: Int = NO_ANIMATION
    ) : NumberBox {
        override val value = EMPTY_VALUE
    }

    sealed interface StarBox : NumberBox

    data class GoldenStarBox(
        override var boxAnimation: Int = NO_ANIMATION
    ) : StarBox{
        override val value = EMPTY_VALUE
    }

    data class SilverStarBox(
        override var boxAnimation: Int = 0
    ) : StarBox {
        override var value: Int = EMPTY_VALUE
            private set

        override fun setValue(newValue: Int) {
            value = newValue
        }
    }

    companion object {
        const val EMPTY_VALUE = -10

        //ANIMACIONES
        const val NO_ANIMATION = 0
        private val AVAILABLE_ANIMATIONS = listOf(
            NO_ANIMATION
        )
    }
}

