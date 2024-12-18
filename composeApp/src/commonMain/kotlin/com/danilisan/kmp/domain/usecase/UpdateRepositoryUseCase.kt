package com.danilisan.kmp.domain.usecase

import com.danilisan.kmp.domain.repository.Repository
import kotlin.reflect.KClass

class UpdateRepositoryUseCase(
    private val repositoryMap: Map<KClass<*>, Repository<*>>
) {
    operator fun <T : Any> invoke(model: T){
        val repository = repositoryMap[model::class] as? Repository<T>
            ?: throw IllegalArgumentException("No repository found for ${model::class}")
        repository.updateElement(model)
    }
}