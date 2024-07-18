package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class WalletRepositoryTest {
    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Test
    fun `should return a wallet that has id after saving it`() {
        // given
        val userId = 52L
        val wallet = createWallet(userId, BigDecimal.ZERO)

        // when
        val savedOne = walletRepository.save(wallet)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(0L)
        Assertions.assertThat(savedOne.userId).isEqualTo(userId)
        Assertions.assertThat(savedOne.balance).isEqualByComparingTo(BigDecimal.ZERO)
        Assertions.assertThat(savedOne.createdAt).isNotNull()
        Assertions.assertThat(savedOne.updatedAt).isNotNull()
    }

    @Test
    fun `should return found wallet that has userId`() {
        // given
        val userId = 4L
        val balance = BigDecimal(13000)
        val wallet = createWallet(userId, balance)
        walletRepository.save(wallet)

        // when
        val foundOne = walletRepository.findOneByUserIdWithLock(userId)

        // then
        Assertions.assertThat(foundOne).isNotNull()
        Assertions.assertThat(foundOne!!.id).isNotEqualTo(0L)
        Assertions.assertThat(foundOne.userId).isEqualTo(userId)
        Assertions.assertThat(foundOne.balance).isEqualByComparingTo(balance)
    }

    private fun createWallet(
        userId: Long,
        balance: BigDecimal,
    ): Wallet =
        Wallet(
            id = 0L,
            userId = userId,
            balance = balance,
            createdAt = ZonedDateTime.now().asUtc,
            updatedAt = ZonedDateTime.now().asUtc,
        )
}
