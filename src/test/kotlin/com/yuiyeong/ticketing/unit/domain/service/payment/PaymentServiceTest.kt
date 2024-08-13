package com.yuiyeong.ticketing.unit.domain.service.payment

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.event.payment.PaymentEventPublisher
import com.yuiyeong.ticketing.domain.model.payment.Payment
import com.yuiyeong.ticketing.domain.model.payment.PaymentStatus
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.model.wallet.TransactionType
import com.yuiyeong.ticketing.domain.repository.payment.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.domain.service.payment.PaymentService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {
    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var paymentEventPublisher: PaymentEventPublisher

    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun beforeEach() {
        paymentService =
            PaymentService(reservationRepository, transactionRepository, paymentRepository, paymentEventPublisher)
    }

    @Test
    fun `should return Payment of which status is success`() {
        // given
        val userId = 3L
        val reservation = createReservation(userId)
        given(reservationRepository.findOneById(reservation.id)).willReturn(reservation)

        val transaction = createTransaction(reservation.totalAmount)
        given(transactionRepository.findOneById(transaction.id)).willReturn(transaction)

        given(paymentRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Payment>(0)
            savedOne.copy(id = 1L)
        }

        // when
        val payment = paymentService.create(userId, reservation.id, transaction.id, null)

        // then
        Assertions.assertThat(payment.userId).isEqualTo(userId)
        Assertions.assertThat(payment.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(payment.transactionId).isEqualTo(transaction.id)
        Assertions.assertThat(payment.failureReason).isNull()
        Assertions.assertThat(payment.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(payment.status).isEqualTo(PaymentStatus.COMPLETED)

        verify(reservationRepository).findOneById(reservation.id)
        verify(transactionRepository).findOneById(transaction.id)
        verify(paymentRepository).save(
            argThat { it ->
                it.userId == userId &&
                    it.reservationId == reservation.id &&
                    it.transactionId == transaction.id &&
                    it.failureReason == null
            },
        )
    }

    @Test
    fun `should return Payment of which status is failed`() {
        // given
        val userId = 5L
        val failureReason = "InsufficientBalance"
        val reservation = createReservation(userId)
        given(reservationRepository.findOneById(reservation.id)).willReturn(reservation)
        given(paymentRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Payment>(0)
            savedOne.copy(id = 1L)
        }

        // when
        val payment = paymentService.create(userId, reservation.id, null, failureReason)

        // then
        Assertions.assertThat(payment.userId).isEqualTo(userId)
        Assertions.assertThat(payment.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(payment.transactionId).isNull()
        Assertions.assertThat(payment.failureReason).isEqualTo(failureReason)
        Assertions.assertThat(payment.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(payment.status).isEqualTo(PaymentStatus.FAILED)

        verify(reservationRepository).findOneById(reservation.id)
        verify(paymentRepository).save(
            argThat { it ->
                it.userId == userId &&
                    it.reservationId == reservation.id &&
                    it.transactionId == null &&
                    it.failureReason == failureReason
            },
        )
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
