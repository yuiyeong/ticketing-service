package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyCanceledException
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import org.assertj.core.api.Assertions
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

class ReservationTest {
    @Test
    fun `should confirm a reservation`() {
        // given
        val reservation = createReservation(ReservationStatus.PENDING)

        // when
        reservation.confirm()

        // then
        Assertions.assertThat(reservation.status).isEqualTo(ReservationStatus.CONFIRMED)
    }

    @Test
    fun `should throw ReservationAlreadyConfirmedException when trying to confirm confirmed Reservation`() {
        // given
        val reservation = createReservation(ReservationStatus.CONFIRMED)

        // when & then
        Assertions
            .assertThatThrownBy { reservation.confirm() }
            .isInstanceOf(ReservationAlreadyConfirmedException::class.java)
    }

    @Test
    fun `should throw ReservationAlreadyConfirmedException when trying to confirm canceled Reservation`() {
        // given
        val reservation = createReservation(ReservationStatus.CANCELLED)

        // when & then
        Assertions
            .assertThatThrownBy { reservation.confirm() }
            .isInstanceOf(ReservationAlreadyCanceledException::class.java)
    }

    private fun createReservation(status: ReservationStatus): Reservation =
        Reservation(
            id = 31L,
            userId = 12L,
            concertId = 2L,
            concertEventId = 3L,
            status = status,
            seatIds = listOf(11L),
            totalSeats = 1,
            totalAmount = BigDecimal(20000),
            createdAt = ZonedDateTime.now(),
        )
}
