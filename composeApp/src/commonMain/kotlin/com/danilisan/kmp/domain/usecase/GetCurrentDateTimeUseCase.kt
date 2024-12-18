package com.danilisan.kmp.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns date and time when it's called
 * in LocalDateTime format
 */
class GetCurrentDateTimeUseCase {
    operator fun invoke(): LocalDateTime =
        Clock.System.now()
            .toLocalDateTime(
                TimeZone.currentSystemDefault())
}