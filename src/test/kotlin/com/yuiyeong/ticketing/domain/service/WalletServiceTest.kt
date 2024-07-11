package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.exception.NotFoundWalletException
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class WalletServiceTest {
    @Mock
    private lateinit var walletRepository: WalletRepository

    private lateinit var walletService: WalletService

    @BeforeEach
    fun beforeEach() {
        walletService = WalletService(walletRepository)
    }

    @Test
    fun `should return wallet with charged balance`() {
        // given
        val userId = 1L
        val balance = BigDecimal(100)
        val amount = 1000L
        val wallet = Wallet(1L, userId, balance, ZonedDateTime.now(), ZonedDateTime.now())
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)
        given(walletRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Wallet>(0)
            savedOne.copy(id = 1L) // Simulate ID assignment
        }

        // when
        val chargedOne = walletService.charge(userId, amount)

        // then
        Assertions.assertThat(chargedOne.balance).isEqualTo(balance + BigDecimal(amount))
        verify(walletRepository).findOneByUserId(userId)
    }

    @Test
    fun `should throw InvalidAmountException for negative amount`() {
        // given
        val userId = 1L
        val balance = BigDecimal(100)
        val amount = -1000L
        val wallet = Wallet(1L, userId, balance, ZonedDateTime.now(), ZonedDateTime.now())
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)

        // when & then
        Assertions
            .assertThatThrownBy { walletService.charge(userId, amount) }
            .isInstanceOf(InvalidAmountException::class.java)
        verify(walletRepository).findOneByUserId(userId)
    }

    @Test
    fun `should return wallet of user`() {
        // given
        val userId = 21L
        val balance = BigDecimal(2301L)
        val wallet = Wallet(1L, userId, balance, ZonedDateTime.now(), ZonedDateTime.now())
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)

        // when
        val dto = walletService.getBalance(userId)

        // then
        Assertions.assertThat(dto.balance).isEqualTo(balance)
        verify(walletRepository).findOneByUserId(userId)
    }

    @Test
    fun `should throw NotFoundWalletException when unknown userId`() {
        // given
        val unknownUserId = 213L

        // when & then
        Assertions
            .assertThatThrownBy { walletService.getBalance(unknownUserId) }
            .isInstanceOf(NotFoundWalletException::class.java)
    }
}
