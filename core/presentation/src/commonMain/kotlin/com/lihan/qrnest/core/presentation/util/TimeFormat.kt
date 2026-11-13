package com.lihan.qrnest.core.presentation.util

import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeFormat {

    fun toFormattedTime(
        timestamp: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val dateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
        val day = dateTime.day.toString().padStart(2, '0')
        val month = dateTime.month.shortName()
        val year = dateTime.year
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$day $month $year, $hour:$minute"
    }

    private fun Month.shortName(): String {
        return when (this) {
            Month.JANUARY -> "Jan"
            Month.FEBRUARY -> "Feb"
            Month.MARCH -> "Mar"
            Month.APRIL -> "Apr"
            Month.MAY -> "May"
            Month.JUNE -> "Jun"
            Month.JULY -> "Jul"
            Month.AUGUST -> "Aug"
            Month.SEPTEMBER -> "Sep"
            Month.OCTOBER -> "Oct"
            Month.NOVEMBER -> "Nov"
            Month.DECEMBER -> "Dec"
        }
    }
}
