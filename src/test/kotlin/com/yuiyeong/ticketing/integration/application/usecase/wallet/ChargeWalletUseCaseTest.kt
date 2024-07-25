package com.yuiyeong.ticketing.integration.application.usecase.wallet

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.wallet.ChargeWalletUseCase
import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.repository.wallet.WalletRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.Test

@Transactional
@SpringBootTest
class ChargeWalletUseCaseTest {
    @Autowired
    private lateinit var chargeWalletUseCase: ChargeWalletUseCase

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Test
    fun `should return charged wallet`() {
        // given
        val userId = 765L
        val amount = 300000L
        val originalWallet = walletRepository.save(TestDataFactory.createWallet(userId = userId, balance = BigDecimal.ONE))

        // when
        val result = chargeWalletUseCase.execute(userId, amount)

        // then
        Assertions.assertThat(result.userId).isEqualTo(userId)
        Assertions.assertThat(result.balance).isEqualByComparingTo(originalWallet.balance + BigDecimal(amount))
    }

    @Test
    fun `should throw WalletNotFoundException when trying to charge with unknown userId`() {
        // given
        val unknownUserId = 75L

        // when & then
        Assertions
            .assertThatThrownBy {
                chargeWalletUseCase.execute(unknownUserId, 23423L)
            }.isInstanceOf(WalletNotFoundException::class.java)
    }

    @Test
    fun `should throw InvalidAmountException when trying to charge with negative amount`() {
        // given
        val userId = 5L
        val amount = -3000L
        walletRepository.save(TestDataFactory.createWallet(userId = userId, balance = BigDecimal.ONE))

        // when & then
        Assertions
            .assertThatThrownBy {
                chargeWalletUseCase.execute(userId, amount)
            }.isInstanceOf(InvalidAmountException::class.java)
    }
}
