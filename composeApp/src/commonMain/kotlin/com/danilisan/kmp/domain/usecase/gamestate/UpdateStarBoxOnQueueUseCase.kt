package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class UpdateStarBoxOnQueueUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        queue: List<NumberBox>,
        newBox: NumberBox,
    ): List<NumberBox> = withContext(dispatcher.default) {
//        if(queue.isEmpty()){
//            queue
//        }else{
//            queue.toMutableList().apply{
//                set(0, newBox)
//            }
//        }
        queue.toMutableList().apply {
            if(lastIndex < 0){
                add(newBox)
            }else{
                this[lastIndex] = newBox
            }
        }
    }
}