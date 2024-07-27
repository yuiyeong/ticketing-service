package com.yuiyeong.ticketing.integration.concurrent

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.wallet.WalletRepository
import com.yuiyeong.ticketing.domain.service.wallet.WalletService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.test.Test

@SpringBootTest
class WalletServiceConcurrentTest {
    @Autowired
    private lateinit var walletService: WalletService

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    private val userId = 742L
    private val initialBalance = BigDecimal(10_000)
    private val logger = LoggerFactory.getLogger("지갑 동시성 테스트")

    @BeforeEach
    fun beforeEach() {
        // given: 잔고가 10,000 인 지갑
        walletRepository.save(TestDataFactory.createWallet(userId = userId, balance = initialBalance))
    }

    @Test
    fun `should handle concurrent charge and pay correctly`() {
        // given
        val taskCount = 9 // 3으로 나누어 떨어질 수 있도록
        val latch = CountDownLatch(taskCount)
        val executorService = Executors.newFixedThreadPool(taskCount)

        val chargingAmount = BigDecimal(1000L)
        val payingAmount = BigDecimal(750L)
        val maxWorkingTime = AtomicReference(0L)

        // when: 나머지에 따라, 충전, 사용, 충전 및 사용을 진행한다.
        for (i in 1..taskCount) {
            executorService.submit {
                val startTime = System.nanoTime()
                try {
                    when (i % 3) {
                        0 -> walletService.charge(userId, chargingAmount)
                        1 -> walletService.pay(userId, payingAmount)
                        2 -> {
                            walletService.charge(userId, chargingAmount)
                            walletService.pay(userId, payingAmount)
                        }
                    }
                } finally {
                    val threadTime = System.nanoTime() - startTime
                    maxWorkingTime.updateAndGet { max(it, threadTime) }
                    latch.countDown()
                }
            }
        }
        latch.await()
        logger.info("가장 오래 걸린 시간: ${TimeUnit.NANOSECONDS.toMillis(maxWorkingTime.get())} ms")

        // then: 최종 잔액은 처음 잔액에서 총 충전 금액을 더하고 총 사용 금액을 뺀 값이다.
        val executedCount = BigDecimal((taskCount / 3) * 2)
        val expectedBalance =
            initialBalance
                .add(chargingAmount.multiply(executedCount))
                .subtract(payingAmount.multiply(executedCount))

        val wallet = walletService.getUserWallet(userId)
        Assertions.assertThat(wallet.balance).isEqualByComparingTo(expectedBalance)

        executorService.shutdown()
    }
}
