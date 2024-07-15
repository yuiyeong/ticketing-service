package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyCanceledException
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Reservation(
    val id: Long,
    val userId: Long,
    val concertId: Long,
    val concertEventId: Long,
    var status: ReservationStatus,
    val seatIds: List<Long>,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    fun confirm() {
        verifyStatusIsPending()

        status = ReservationStatus.CONFIRMED
    }

    private fun verifyStatusIsPending() {
        if (status == ReservationStatus.CONFIRMED) {
            throw ReservationAlreadyConfirmedException()
        }

        if (status == ReservationStatus.CANCELLED) {
            throw ReservationAlreadyCanceledException()
        }
    }

    companion object {
        fun create(
            userId: Long,
            concertEvent: ConcertEvent,
            occupiedSeats: List<Seat>,
        ): Reservation =
            Reservation(
                id = 0L,
                userId = userId,
                concertId = concertEvent.concert.id,
                concertEventId = concertEvent.id,
                status = ReservationStatus.PENDING,
                seatIds = occupiedSeats.map { it.id },
                totalSeats = occupiedSeats.count(),
                totalAmount = occupiedSeats.sumOf { it.price },
                createdAt = ZonedDateTime.now(),
            )
    }
}

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
}
