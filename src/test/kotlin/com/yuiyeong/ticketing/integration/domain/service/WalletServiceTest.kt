package com.yuiyeong.ticketing.integration.domain.service
import com.yuiyeong.ticketing.TestDataFactory.createWallet
import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.model.TransactionType
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import com.yuiyeong.ticketing.domain.service.WalletService
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
import java.math.BigDecimal
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class WalletServiceTest {
    @Autowired
    private lateinit var walletService: WalletService

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @AfterEach
    fun afterEach() {
        transactionRepository.deleteAll()
        walletRepository.deleteAll()
    }

    @Nested
    inner class GetUserWalletTest {
        @Test
        fun `should return user wallet when it exists`() {
            // given
            val userId = 1L
            val wallet = createWallet(userId = userId)
            walletRepository.save(wallet)

            // when
            val result = walletService.getUserWallet(userId)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.balance).isEqualTo(wallet.balance)
        }

        @Test
        fun `should throw exception when wallet is not found`() {
            // given
            val nonExistentUserId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    walletService.getUserWallet(nonExistentUserId)
                }.isInstanceOf(WalletNotFoundException::class.java)
        }
    }

    @Nested
    inner class ChargeTest {
        @Test
        fun `should charge wallet successfully`() {
            // given
            val userId = 1L
            val initialBalance = BigDecimal(100)
            val chargeAmount = BigDecimal(50)
            val wallet = walletRepository.save(createWallet(userId = userId, balance = initialBalance))

            // when
            val result = walletService.charge(userId, chargeAmount)

            // then
            Assertions.assertThat(result.walletId).isEqualTo(wallet.id)
            Assertions.assertThat(result.amount).isEqualTo(chargeAmount)
            Assertions.assertThat(result.type).isEqualTo(TransactionType.CHARGE)

            val updatedWallet = walletService.getUserWallet(userId)
            Assertions.assertThat(updatedWallet.balance).isEqualTo(initialBalance + chargeAmount)
        }

        @Test
        fun `should throw exception when charging with invalid amount`() {
            // given
            val userId = 1L
            val invalidAmount = BigDecimal(-50)
            val wallet = createWallet(userId = userId)
            walletRepository.save(wallet)

            // when & then
            Assertions
                .assertThatThrownBy {
                    walletService.charge(userId, invalidAmount)
                }.isInstanceOf(InvalidAmountException::class.java)
        }
    }

    @Nested
    inner class PayTest {
        @Test
        fun `should pay from wallet successfully`() {
            // given
            val userId = 1L
            val initialBalance = BigDecimal(100)
            val payAmount = BigDecimal(50)
            val wallet = walletRepository.save(createWallet(userId = userId, balance = initialBalance))

            // when
            val result = walletService.pay(userId, payAmount)

            // then
            Assertions.assertThat(result.walletId).isEqualTo(wallet.id)
            Assertions.assertThat(result.amount).isEqualTo(payAmount)
            Assertions.assertThat(result.type).isEqualTo(TransactionType.PAYMENT)

            val updatedWallet = walletService.getUserWallet(userId)
            Assertions.assertThat(updatedWallet.balance).isEqualTo(initialBalance - payAmount)
        }

        @Test
        fun `should throw exception when paying with invalid amount`() {
            // given
            val userId = 1L
            val invalidAmount = BigDecimal(-50)
            val wallet = createWallet(userId = userId)
            walletRepository.save(wallet)

            // when & then
            Assertions
                .assertThatThrownBy {
                    walletService.pay(userId, invalidAmount)
                }.isInstanceOf(InvalidAmountException::class.java)
        }

        @Test
        fun `should throw exception when paying with insufficient balance`() {
            // given
            val userId = 1L
            val initialBalance = BigDecimal(100)
            val payAmount = BigDecimal(150)
            val wallet = createWallet(userId = userId, balance = initialBalance)
            walletRepository.save(wallet)

            // when & then
            Assertions
                .assertThatThrownBy {
                    walletService.pay(userId, payAmount)
                }.isInstanceOf(InsufficientBalanceException::class.java)
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("wallet_service_test_db")
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
