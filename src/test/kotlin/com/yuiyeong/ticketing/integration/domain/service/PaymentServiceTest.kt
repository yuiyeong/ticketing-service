package com.yuiyeong.ticketing.integration.domain.service

import com.yuiyeong.ticketing.TestDataFactory.createPayment
import com.yuiyeong.ticketing.TestDataFactory.createReservation
import com.yuiyeong.ticketing.TestDataFactory.createTransaction
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.TransactionNotFoundException
import com.yuiyeong.ticketing.domain.model.payment.PaymentMethod
import com.yuiyeong.ticketing.domain.model.payment.PaymentStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.domain.service.payment.PaymentService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class PaymentServiceTest {
    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @AfterEach
    fun afterEach() {
        paymentRepository.deleteAll()
        transactionRepository.deleteAll()
        reservationRepository.deleteAll()
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `should create payment successfully with transaction`() {
            // given
            val userId = 1L
            val reservation = createReservation(userId = userId)
            val savedReservation = reservationRepository.save(reservation)

            val transaction = createTransaction(walletId = userId)
            val savedTransaction = transactionRepository.save(transaction)

            // when
            val result = paymentService.create(userId, savedReservation.id, savedTransaction.id, null)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.reservationId).isEqualTo(savedReservation.id)
            Assertions.assertThat(result.transactionId).isEqualTo(savedTransaction.id)
            Assertions.assertThat(result.amount).isEqualTo(savedReservation.totalAmount)
            Assertions.assertThat(result.status).isEqualTo(PaymentStatus.COMPLETED)
            Assertions.assertThat(result.paymentMethod).isEqualTo(PaymentMethod.WALLET)
            Assertions.assertThat(result.failureReason).isNull()
        }

        @Test
        fun `should create failed payment without transaction`() {
            // given
            val userId = 1L
            val reservation = createReservation(userId = userId)
            val savedReservation = reservationRepository.save(reservation)
            val failureReason = "Insufficient balance"

            // when
            val result = paymentService.create(userId, savedReservation.id, null, failureReason)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.reservationId).isEqualTo(savedReservation.id)
            Assertions.assertThat(result.transactionId).isNull()
            Assertions.assertThat(result.amount).isEqualTo(savedReservation.totalAmount)
            Assertions.assertThat(result.status).isEqualTo(PaymentStatus.FAILED)
            Assertions.assertThat(result.paymentMethod).isEqualTo(PaymentMethod.WALLET)
            Assertions.assertThat(result.failureReason).isEqualTo(failureReason)
        }

        @Test
        fun `should throw exception when reservation is not found`() {
            // given
            val userId = 1L
            val nonExistentReservationId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    paymentService.create(userId, nonExistentReservationId, null, null)
                }.isInstanceOf(ReservationNotFoundException::class.java)
        }

        @Test
        fun `should throw exception when transaction is not found`() {
            // given
            val userId = 1L
            val reservation = createReservation(userId = userId)
            val savedReservation = reservationRepository.save(reservation)
            val nonExistentTransactionId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    paymentService.create(userId, savedReservation.id, nonExistentTransactionId, null)
                }.isInstanceOf(TransactionNotFoundException::class.java)
        }
    }

    @Nested
    inner class GetHistoryTest {
        @Test
        fun `should return payment history for user`() {
            // given
            val userId = 1L
            val payments =
                listOf(
                    createPayment(userId = userId),
                    createPayment(userId = userId),
                    createPayment(userId = userId),
                )
            paymentRepository.saveAll(payments)

            // when
            val result = paymentService.getHistory(userId)

            // then
            Assertions.assertThat(result).hasSize(3)
            Assertions.assertThat(result).allMatch { it.userId == userId }
        }

        @Test
        fun `should return empty list when user has no payment history`() {
            // given
            val userId = 1L

            // when
            val result = paymentService.getHistory(userId)

            // then
            Assertions.assertThat(result).isEmpty()
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("payment_service_test_db")
                withUsername("testuser")
                withPassword("testpass")
                withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--default-time-zone=+00:00",
                )
                withEnv("TZ", "UTC")
                withReuse(true)
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                "${mysqlContainer.jdbcUrl}?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
            }
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
        }
    }
}
