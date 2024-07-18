package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.PaymentMethod
import com.yuiyeong.ticketing.domain.model.PaymentStatus
import com.yuiyeong.ticketing.domain.repository.PaymentRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class PaymentRepositoryTest {
    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Test
    fun `should return a payment that has new id after saving it`() {
        // given
        val userId = 12L
        val transactionId = 12L
        val payment = createPayment(userId, PaymentStatus.COMPLETED, transactionId)

        // when
        val savedOne = paymentRepository.save(payment)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(payment.id)
        Assertions.assertThat(savedOne.userId).isEqualTo(payment.userId)
        Assertions.assertThat(savedOne.status).isEqualTo(payment.status)
        Assertions.assertThat(savedOne.transactionId).isEqualTo(payment.transactionId)
        Assertions.assertThat(savedOne.failureReason).isNull()
        Assertions.assertThat(savedOne.createdAt).isNotNull()
        Assertions.assertThat(savedOne.updatedAt).isNotNull()
    }

    @Test
    fun `should return found payment that the id`() {
        // given
        val userOneId = 12L
        val userTwoId = 23L
        val userOnePayment = paymentRepository.save(createPayment(userOneId, PaymentStatus.COMPLETED, 3L))
        val userTwoPayment =
            paymentRepository.save(createPayment(userTwoId, PaymentStatus.FAILED, failureReason = "test"))

        // when
        val foundUserOnes = paymentRepository.findAllByUserId(userOneId)
        val foundUserTwos = paymentRepository.findAllByUserId(userTwoId)
        val foundNoOnes = paymentRepository.findAllByUserId(22L)

        // then
        Assertions.assertThat(foundUserOnes.count()).isEqualTo(1)
        Assertions.assertThat(foundUserTwos.count()).isEqualTo(1)
        Assertions.assertThat(foundNoOnes.count()).isEqualTo(0)

        Assertions.assertThat(foundUserOnes[0].id).isEqualTo(userOnePayment.id)
        Assertions.assertThat(foundUserOnes[0].userId).isEqualTo(userOnePayment.userId)
        Assertions.assertThat(foundUserOnes[0].status).isEqualTo(userOnePayment.status)
        Assertions.assertThat(foundUserOnes[0].transactionId).isEqualTo(userOnePayment.transactionId)
        Assertions.assertThat(foundUserOnes[0].failureReason).isNull()

        Assertions.assertThat(foundUserTwos[0].id).isEqualTo(userTwoPayment.id)
        Assertions.assertThat(foundUserTwos[0].userId).isEqualTo(userTwoPayment.userId)
        Assertions.assertThat(foundUserTwos[0].status).isEqualTo(userTwoPayment.status)
        Assertions.assertThat(foundUserTwos[0].transactionId).isNull()
        Assertions.assertThat(foundUserTwos[0].failureReason).isEqualTo(userTwoPayment.failureReason)
    }

    private fun createPayment(
        userId: Long,
        status: PaymentStatus,
        transactionId: Long? = null,
        failureReason: String? = null,
    ): Payment =
        Payment(
            id = 0L,
            userId = userId,
            transactionId = transactionId,
            reservationId = 8L,
            amount = BigDecimal(230000),
            status = status,
            paymentMethod = PaymentMethod.WALLET,
            failureReason = failureReason,
            createdAt = ZonedDateTime.now().asUtc,
            updatedAt = ZonedDateTime.now().asUtc,
        )
}
