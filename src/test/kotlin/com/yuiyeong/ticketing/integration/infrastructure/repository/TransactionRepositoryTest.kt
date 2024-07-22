package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.model.wallet.TransactionType
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class TransactionRepositoryTest {
    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Test
    fun `should return a charged transaction that has new id after saving charged transaction`() {
        // given
        val walletId = 12L
        val amount = BigDecimal(3442)
        val transaction = createTransaction(walletId, amount, TransactionType.CHARGE)

        // when
        val savedOne = transactionRepository.save(transaction)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(0L)
        Assertions.assertThat(savedOne.walletId).isEqualTo(walletId)
        Assertions.assertThat(savedOne.amount).isEqualByComparingTo(amount)
        Assertions.assertThat(savedOne.type).isEqualTo(TransactionType.CHARGE)
        Assertions.assertThat(savedOne.createdAt).isNotNull()
    }

    @Test
    fun `should return found transaction that has the id`() {
        // given
        val walletId = 411L
        val amount = BigDecimal(3000)
        val transaction = createTransaction(walletId, amount, TransactionType.PAYMENT)

        val savedOne = transactionRepository.save(transaction)

        // when
        val foundOne = transactionRepository.findOneById(savedOne.id)

        // then
        Assertions.assertThat(foundOne).isNotNull()
        Assertions.assertThat(foundOne!!.id).isEqualTo(savedOne.id)
        Assertions.assertThat(foundOne.walletId).isEqualTo(walletId)
        Assertions.assertThat(foundOne.amount).isEqualByComparingTo(amount)
        Assertions.assertThat(foundOne.type).isEqualByComparingTo(TransactionType.PAYMENT)
    }

    private fun createTransaction(
        walletId: Long,
        amount: BigDecimal,
        type: TransactionType,
    ): Transaction =
        Transaction(
            id = 0L,
            walletId = walletId,
            amount = amount,
            type = type,
            createdAt = ZonedDateTime.now().asUtc,
        )
}
