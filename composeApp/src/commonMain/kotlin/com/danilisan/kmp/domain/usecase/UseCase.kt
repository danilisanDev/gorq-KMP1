package com.danilisan.kmp.domain.usecase

import com.danilisan.kmp.core.provider.DispatcherProvider

/**
 * Interface for usecases.
 */

interface UseCase {
    val dispatcher: DispatcherProvider
}

