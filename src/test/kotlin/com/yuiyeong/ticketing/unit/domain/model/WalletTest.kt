package com.yuiyeong.ticketing.unit.domain.model

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.model.Wallet
import org.assertj.core.api.Assertions
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

class WalletTest {
    @Test
    fun `should change a balance of wallet`() {
        // given
        val balance = 92220L
        val wallet = createWallet(balance)
        val amount = BigDecimal(134722)

        // when
        wallet.charge(amount)

        // then
        Assertions.assertThat(wallet.balance).isEqualTo(BigDecimal(balance) + amount)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to charge zero amount`() {
        // given
        val wallet = createWallet(1000)
        val amount = BigDecimal.ZERO

        // when & then
        Assertions
            .assertThatThrownBy { wallet.charge(amount) }
            .isInstanceOf(InvalidAmountException::class.java)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to charge negative amount`() {
        // given
        val wallet = createWallet(1000)
        val amount = BigDecimal(-1000)

        // when & then
        Assertions
            .assertThatThrownBy { wallet.charge(amount) }
            .isInstanceOf(InvalidAmountException::class.java)
    }

    @Test
    fun `should return a wallet that has reduced balance after paying`() {
        // given
        val balance = 90000L
        val wallet = createWallet(balance)
        val amount = BigDecimal(722)

        // when
        wallet.pay(amount)

        // then
        Assertions.assertThat(wallet.balance).isEqualTo(BigDecimal(balance) - amount)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to pay zero amount`() {
        // given
        val wallet = createWallet(1000)
        val amount = BigDecimal.ZERO

        // when & then
        Assertions
            .assertThatThrownBy { wallet.pay(amount) }
            .isInstanceOf(InvalidAmountException::class.java)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to pay negative amount`() {
        // given
        val wallet = createWallet(1000)
        val amount = BigDecimal(-1000)

        // when & then
        Assertions
            .assertThatThrownBy { wallet.pay(amount) }
            .isInstanceOf(InvalidAmountException::class.java)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to pay bigger amount than balance`() {
        // given
        val wallet = createWallet(1000)
        val amount = BigDecimal(1001)

        // when & then
        Assertions
            .assertThatThrownBy { wallet.pay(amount) }
            .isInstanceOf(InsufficientBalanceException::class.java)
    }

    private fun createWallet(balance: Long) =
        Wallet(
            id = 123L,
            userId = 883L,
            balance = BigDecimal(balance),
            createdAt = ZonedDateTime.now().asUtc,
            updatedAt = ZonedDateTime.now().asUtc,
        )
}
