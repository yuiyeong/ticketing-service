package com.yuiyeong.ticketing.common

import java.time.ZoneOffset
import java.time.ZonedDateTime

val ZonedDateTime.asUtc: ZonedDateTime
    get() = withZoneSameInstant(ZoneOffset.UTC)
