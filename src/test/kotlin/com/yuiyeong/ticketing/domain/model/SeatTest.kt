package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.SeatAlreadyUnavailableException
import org.assertj.core.api.Assertions
import java.math.BigDecimal
import kotlin.test.Test

class SeatTest {
    @Test
    fun `should return unavailable seat after makeUnavailable`() {
        // given
        val seat = createSeat(true)

        // when
        seat.makeUnavailable()

        // then
        Assertions.assertThat(seat.isAvailable).isEqualTo(false)
    }

    @Test
    fun `should throw SeatAlreadyUnavailableException when trying to makeUnavailable about an unavailable seat`() {
        // given
        val seat = createSeat(false)

        // when & then
        Assertions
            .assertThatThrownBy { seat.makeUnavailable() }
            .isInstanceOf(SeatAlreadyUnavailableException::class.java)
    }

    private fun createSeat(isAvailable: Boolean): Seat =
        Seat(
            id = 41L,
            concertEventId = 583L,
            seatNumber = "54번",
            price = BigDecimal(43000),
            isAvailable = isAvailable,
        )
}
