package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import org.assertj.core.api.Assertions
import java.time.ZonedDateTime
import kotlin.test.Test

class ConcertEventTest {
    @Test
    fun `should throw ReservationClosedException when the moment is not within period`() {
        // given
        val reservationPeriodStart = ZonedDateTime.now().minusMonths(1)
        val concertEvent = createConcertEvent(reservationPeriodStart)

        val momentNotInPeriod = reservationPeriodStart.minusSeconds(1)

        // when & then
        concertEvent.verifyWithinReservationPeriod(reservationPeriodStart) // pass

        Assertions
            .assertThatThrownBy { concertEvent.verifyWithinReservationPeriod(momentNotInPeriod) }
            .isInstanceOf(ReservationNotOpenedException::class.java)
    }

    private fun createConcertEvent(reservationPeriodStart: ZonedDateTime): ConcertEvent {
        val concert = Concert(1L, "test title", "test singer", "test description")
        return ConcertEvent(
            id = 11L,
            concert = concert,
            venue = "test place",
            reservationPeriod = DateTimeRange(reservationPeriodStart, reservationPeriodStart.plusDays(5)),
            performanceSchedule = DateTimeRange(ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)),
            maxSeatCount = 10,
            availableSeatCount = 2,
        )
    }
}
