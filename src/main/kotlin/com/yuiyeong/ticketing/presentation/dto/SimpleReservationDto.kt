package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.ReservationDto
import com.yuiyeong.ticketing.domain.model.ReservationStatus
import java.math.BigDecimal
import java.time.ZonedDateTime

data class SimpleReservationDto(
    val id: Long,
    val concertPerformanceStartAt: ZonedDateTime,
    val status: ReservationStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(reservationDto: ReservationDto): SimpleReservationDto =
            SimpleReservationDto(
                reservationDto.id,
                reservationDto.concertPerformanceStartAt,
                reservationDto.status,
                reservationDto.totalSeats,
                reservationDto.totalAmount,
                reservationDto.createdAt,
            )
    }
}
