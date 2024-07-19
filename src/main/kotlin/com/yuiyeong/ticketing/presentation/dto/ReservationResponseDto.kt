package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.ReservationResult
import com.yuiyeong.ticketing.domain.model.ReservationStatus
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
