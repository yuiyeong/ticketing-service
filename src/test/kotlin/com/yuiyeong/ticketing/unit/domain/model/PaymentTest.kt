package com.yuiyeong.ticketing.unit.domain.model

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.PaymentStatus
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.model.ReservationStatus
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import org.assertj.core.api.Assertions
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

class PaymentTest {
    @Test
    fun `should return Payment of which status is success`() {
        // given
        val userId = 13L
        val reservation = createReservation(userId)
        val transaction = createTransaction(reservation.totalAmount)

        // when
        val payment = Payment.create(userId, reservation, transaction, null)

        // then
        Assertions.assertThat(payment.userId).isEqualTo(userId)
        Assertions.assertThat(payment.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(payment.transactionId).isEqualTo(transaction.id)
        Assertions.assertThat(payment.failureReason).isNull()
        Assertions.assertThat(payment.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(payment.status).isEqualTo(PaymentStatus.COMPLETED)
    }

    @Test
    fun `should return Payment of which status is failed`() {
        // given
        val userId = 3L
        val reservation = createReservation(userId)
        val failureReason = "InsufficientBalance"

        // when
        val payment = Payment.create(userId, reservation, null, failureReason)

        // then
        Assertions.assertThat(payment.userId).isEqualTo(userId)
        Assertions.assertThat(payment.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(payment.transactionId).isNull()
        Assertions.assertThat(payment.failureReason).isEqualTo(failureReason)
        Assertions.assertThat(payment.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(payment.status).isEqualTo(PaymentStatus.FAILED)
    }

    private fun createReservation(userId: Long) =
        Reservation(
            id = 31L,
            userId = userId,
            concertId = 2L,
            concertEventId = 3L,
            status = ReservationStatus.PENDING,
            totalSeats = 1,
            totalAmount = BigDecimal(20000),
            createdAt = ZonedDateTime.now().asUtc,
        )

    private fun createTransaction(amount: BigDecimal) =
        Transaction(
            id = 1L,
            walletId = 2L,
            amount = amount,
            type = TransactionType.PAYMENT,
            createdAt = ZonedDateTime.now().asUtc,
        )
}
