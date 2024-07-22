package com.yuiyeong.ticketing.application.dto.reservation

import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import java.math.BigDecimal
import java.time.ZonedDateTime

data class ReservationResult(
    val id: Long,
    val userId: Long,
    val concertVenue: String,
    val concertPerformanceStartAt: ZonedDateTime,
    val status: ReservationStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(
            concertEvent: ConcertEvent,
            reservation: Reservation,
        ): ReservationResult =
            ReservationResult(
                reservation.id,
                reservation.userId,
                concertEvent.venue,
                concertEvent.performanceSchedule.start,
                reservation.status,
                reservation.totalSeats,
                reservation.totalAmount,
                reservation.createdAt,
            )
    }
}
