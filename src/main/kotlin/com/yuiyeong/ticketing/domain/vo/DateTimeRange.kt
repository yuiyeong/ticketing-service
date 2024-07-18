package com.yuiyeong.ticketing.domain.vo

import java.time.Duration
import java.time.ZonedDateTime

data class DateTimeRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
    /**
     * 한 시점(moment) 이 start <= moment <= range 인지를 반환
     */
    fun contains(moment: ZonedDateTime): Boolean =
        if (moment.isBefore(start)) {
            false
        } else if (moment.isAfter(end)) {
            false
        } else {
            true
        }

    /**
     * 다른 DateTimeRange 와 겹치는 지를 반환
     */
    fun overlaps(other: DateTimeRange): Boolean =
        if (end.isBefore(other.start)) {
            false
        } else if (start.isAfter(other.end)) {
            false
        } else {
            true
        }

    fun getDurationAsMin(): Long = Duration.between(start, end).toMinutes()
}
