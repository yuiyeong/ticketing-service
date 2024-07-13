package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyCanceledException
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Reservation(
    val id: Long = 0L,
    val userId: Long,
    val concertId: Long,
    val concertEventId: Long,
    var status: ReservationStatus,
    val seats: List<Seat>,
    val createdAt: ZonedDateTime,
) {
    fun confirm() {
        if (status == ReservationStatus.CONFIRMED) {
            throw ReservationAlreadyConfirmedException()
        }

        if (status == ReservationStatus.CANCELLED) {
            throw ReservationAlreadyCanceledException()
        }

        status = ReservationStatus.CONFIRMED
    }

    val totalSeats: Int
        get() = seats.count()

    val totalAmount: BigDecimal
        get() = seats.sumOf { it.price }

    companion object {
        fun create(
            userId: Long,
            concertEvent: ConcertEvent,
            occupation: Occupation,
        ): Reservation =
            Reservation(
                userId = userId,
                concertId = concertEvent.concertId,
                concertEventId = concertEvent.id,
                status = ReservationStatus.PENDING,
                seats = listOf(occupation.seat),
                createdAt = ZonedDateTime.now(),
            )
    }
}

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
}
