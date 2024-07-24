package com.yuiyeong.ticketing.domain.model.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Reservation(
    val id: Long,
    val userId: Long,
    val concertId: Long,
    val concertEventId: Long,
    val status: ReservationStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    val createdAt: ZonedDateTime,
) {
    fun confirm(): Reservation {
        verifyStatusIsNotConfirmed()

        return copy(status = ReservationStatus.CONFIRMED)
    }

    fun markAsPaymentFailed(): Reservation {
        verifyStatusIsNotConfirmed()

        return copy(status = ReservationStatus.PAYMENT_FAILED)
    }

    private fun verifyStatusIsNotConfirmed() {
        if (status == ReservationStatus.CONFIRMED) {
            throw ReservationAlreadyConfirmedException()
        }
    }

    companion object {
        fun create(
            userId: Long,
            concertEvent: ConcertEvent,
            occupation: Occupation,
        ): Reservation =
            Reservation(
                id = 0L,
                userId = userId,
                concertId = concertEvent.concert.id,
                concertEventId = concertEvent.id,
                status = ReservationStatus.PENDING,
                totalSeats = occupation.totalSeats,
                totalAmount = occupation.totalAmount,
                createdAt = ZonedDateTime.now().asUtc,
            )
    }
}

enum class ReservationStatus {
    PENDING,
    PAYMENT_FAILED,
    CONFIRMED,
}
