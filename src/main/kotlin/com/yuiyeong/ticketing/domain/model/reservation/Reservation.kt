package com.yuiyeong.ticketing.domain.model.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import java.math.BigDecimal
import java.time.ZonedDateTime

class Reservation(
    val id: Long,
    val userId: Long,
    val concertId: Long,
    val concertEventId: Long,
    status: ReservationStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    var status: ReservationStatus = status
        private set

    fun confirm() {
        verifyStatusIsNotConfirmed()

        status = ReservationStatus.CONFIRMED
    }

    fun markAsPaymentFailed() {
        verifyStatusIsNotConfirmed()

        status = ReservationStatus.PAYMENT_FAILED
    }

    private fun verifyStatusIsNotConfirmed() {
        if (status == ReservationStatus.CONFIRMED) {
            throw ReservationAlreadyConfirmedException()
        }
    }

    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        concertId: Long = this.concertId,
        concertEventId: Long = this.concertEventId,
        status: ReservationStatus = this.status,
        totalSeats: Int = this.totalSeats,
        totalAmount: BigDecimal = this.totalAmount,
        createdAt: ZonedDateTime = this.createdAt,
    ): Reservation =
        Reservation(
            id = id,
            userId = userId,
            concertId = concertId,
            concertEventId = concertEventId,
            status = status,
            totalSeats = totalSeats,
            totalAmount = totalAmount,
            createdAt = createdAt,
        )

    companion object {
        fun create(
            userId: Long,
            concertEvent: ConcertEvent,
            allocations: List<SeatAllocation>,
        ): Reservation =
            Reservation(
                id = 0L,
                userId = userId,
                concertId = concertEvent.concert.id,
                concertEventId = concertEvent.id,
                status = ReservationStatus.PENDING,
                totalSeats = allocations.count(),
                totalAmount = allocations.sumOf { it.seatPrice },
                createdAt = ZonedDateTime.now().asUtc,
            )
    }
}

enum class ReservationStatus {
    PENDING,
    PAYMENT_FAILED,
    CONFIRMED,
}
