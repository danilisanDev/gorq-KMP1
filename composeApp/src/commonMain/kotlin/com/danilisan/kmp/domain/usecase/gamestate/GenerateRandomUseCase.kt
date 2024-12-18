package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.domain.usecase.GenerationParams
import com.danilisan.kmp.domain.usecase.UseCase

interface GenerateRandomUseCase<R>: UseCase<GenerationParams<R>, Collection<R>>

