package com.danilisan.kmp.domain.usecase

interface UseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}