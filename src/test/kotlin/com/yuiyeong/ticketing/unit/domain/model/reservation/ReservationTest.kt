package com.yuiyeong.ticketing.unit.domain.model.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
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
        val result = reservation.confirm()

        // then
        Assertions.assertThat(result.status).isEqualTo(ReservationStatus.CONFIRMED)
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
    fun `should throw ReservationAlreadyConfirmedException when trying to mark as failed about confirmed Reservation`() {
        // given
        val reservation = createReservation(ReservationStatus.CONFIRMED)

        // when & then
        Assertions
            .assertThatThrownBy { reservation.markAsPaymentFailed() }
            .isInstanceOf(ReservationAlreadyConfirmedException::class.java)
    }

    private fun createReservation(status: ReservationStatus): Reservation =
        Reservation(
            id = 31L,
            userId = 12L,
            concertId = 2L,
            concertEventId = 3L,
            status = status,
            totalSeats = 1,
            totalAmount = BigDecimal(20000),
            createdAt = ZonedDateTime.now().asUtc,
        )
}
