package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.model.ReservationStatus
import java.math.BigDecimal
import java.time.ZonedDateTime

data class ReservationDto(
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
        ): ReservationDto =
            ReservationDto(
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
