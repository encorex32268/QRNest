package com.lihan.qrnest.core.presentation.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test

class TimeFormatTest {

    @Test
    fun `formats UTC instant in dd MMM yyyy HH_mm pattern`() {
        val instant = LocalDateTime(2025, 11, 5, 9, 4).toInstant(TimeZone.UTC)

        val formatted = TimeFormat.toFormattedTime(
            timestamp = instant.toEpochMilliseconds(),
            timeZone = TimeZone.UTC
        )

        assertThat(formatted).isEqualTo("05 Nov 2025, 09:04")
    }

    @Test
    fun `formats leap day and midnight boundary accurately`() {
        val leapMidnight = LocalDateTime(2024, 2, 29, 0, 0).toInstant(TimeZone.UTC)

        val formatted = TimeFormat.toFormattedTime(
            timestamp = leapMidnight.toEpochMilliseconds(),
            timeZone = TimeZone.UTC
        )

        assertThat(formatted).isEqualTo("29 Feb 2024, 00:00")
    }

    @Test
    fun `formats end of year evening timestamp accurately`() {
        val decEvening = LocalDateTime(2026, 12, 31, 23, 59).toInstant(TimeZone.UTC)

        val formatted = TimeFormat.toFormattedTime(
            timestamp = decEvening.toEpochMilliseconds(),
            timeZone = TimeZone.UTC
        )

        assertThat(formatted).isEqualTo("31 Dec 2026, 23:59")
    }
}
