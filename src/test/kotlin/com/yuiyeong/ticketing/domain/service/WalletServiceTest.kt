package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository
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
class WalletServiceTest {
    @Mock
    private lateinit var walletRepository: WalletRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var walletService: WalletService

    @BeforeEach
    fun beforeEach() {
        walletService = WalletService(walletRepository, transactionRepository)
    }

    @Test
    fun `should return charge type transaction after charging`() {
        // given
        val userId = 3L
        val balance = 20L
        val amount = BigDecimal(1000)
        val wallet = createWallet(3L, userId, balance)
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)
        given(transactionRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Transaction>(0)
            savedOne.copy(id = 1L)
        }
        given(walletRepository.save(any())).willAnswer { invocation -> invocation.getArgument<Wallet>(0) }

        // when
        val transaction = walletService.charge(userId, amount)

        // then
        Assertions.assertThat(transaction.type).isEqualTo(TransactionType.CHARGE)
        Assertions.assertThat(transaction.amount).isEqualTo(amount)
        Assertions.assertThat(wallet.balance).isEqualTo(BigDecimal(balance) + amount)

        verify(walletRepository).findOneByUserId(userId)
        verify(transactionRepository).save(argThat { it -> it.walletId == wallet.id })
        verify(walletRepository).save(argThat { it -> it.userId == userId })
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to charge negative amount`() {
        // given
        val userId = 31L
        val amount = BigDecimal(-1000)
        val wallet = createWallet(2L, userId, 10000L)
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)

        // when & then
        Assertions
            .assertThatThrownBy { walletService.charge(userId, amount) }
            .isInstanceOf(InvalidAmountException::class.java)
        verify(walletRepository).findOneByUserId(userId)
    }

    @Test
    fun `should return payment type transaction after paying`() {
        // given
        val userId = 87L
        val balance = 200000L
        val amount = BigDecimal(10000)
        val wallet = createWallet(65L, userId, balance)
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)
        given(transactionRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Transaction>(0)
            savedOne.copy(id = 2L)
        }
        given(walletRepository.save(any())).willAnswer { invocation -> invocation.getArgument<Wallet>(0) }

        // when
        val transaction = walletService.pay(userId, amount)

        // then
        Assertions.assertThat(transaction.type).isEqualTo(TransactionType.PAYMENT)
        Assertions.assertThat(transaction.amount).isEqualTo(amount)
        Assertions.assertThat(wallet.balance).isEqualTo(BigDecimal(balance) - amount)

        verify(walletRepository).findOneByUserId(userId)
        verify(transactionRepository).save(argThat { it -> it.walletId == wallet.id })
        verify(walletRepository).save(argThat { it -> it.userId == userId })
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to pay negative amount`() {
        // given
        val userId = 313L
        val amount = BigDecimal(-1000)
        val wallet = createWallet(21L, userId, 10000L)
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)

        // when & then
        Assertions
            .assertThatThrownBy { walletService.pay(userId, amount) }
            .isInstanceOf(InvalidAmountException::class.java)
        verify(walletRepository).findOneByUserId(userId)
    }

    @Test
    fun `should throw InvalidChargeAmountException when trying to pay bigger amount than balance`() {
        // given
        val userId = 76L
        val amount = BigDecimal(10000)
        val wallet = createWallet(67L, userId, 1000L)
        given(walletRepository.findOneByUserId(userId)).willReturn(wallet)

        // when & then
        Assertions
            .assertThatThrownBy { walletService.pay(userId, amount) }
            .isInstanceOf(InsufficientBalanceException::class.java)
        verify(walletRepository).findOneByUserId(userId)
    }

    private fun createWallet(
        id: Long,
        userId: Long,
        balance: Long,
    ) = Wallet(
        id = id,
        userId = userId,
        balance = BigDecimal(balance),
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )
}
