package com.danilisan.kmp.domain.usecase

/**
 * Interface for usecases.
 * Recieves in params type P
 * Returns out type R
 */

interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

