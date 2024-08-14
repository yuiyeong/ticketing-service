package com.yuiyeong.ticketing.interfaces.api.dto.reservation

import com.yuiyeong.ticketing.application.dto.reservation.ReservationResult
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import java.math.BigDecimal
import java.time.ZonedDateTime

data class ReservationResponseDto(
    val id: Long,
    val concertPerformanceStartAt: ZonedDateTime,
    val status: ReservationStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(reservationResult: ReservationResult): ReservationResponseDto =
            ReservationResponseDto(
                reservationResult.id,
                reservationResult.concertPerformanceStartAt,
                reservationResult.status,
                reservationResult.totalSeats,
                reservationResult.totalAmount,
                reservationResult.createdAt,
            )
    }
}
