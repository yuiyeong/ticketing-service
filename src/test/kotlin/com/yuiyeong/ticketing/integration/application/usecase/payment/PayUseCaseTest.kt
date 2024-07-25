package com.yuiyeong.ticketing.integration.application.usecase.payment

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.payment.PayUseCase
import com.yuiyeong.ticketing.domain.model.payment.PaymentStatus
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.wallet.TransactionType
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.wallet.WalletRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.Test

@Transactional
@SpringBootTest
class PayUseCaseTest {
    @Autowired
    private lateinit var payUseCase: PayUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var queueEntryRepository: QueueEntryRepository

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var queueEntry: QueueEntry
    private lateinit var reservation: Reservation

    @BeforeEach
    fun setUp() {
        // given: processing 이 queueEntry 와 pending 인 reservation
        val userId = 83L
        queueEntry = queueEntryRepository.save(TestDataFactory.createProcessingQueueEntry(userId))

        val concert = concertRepository.save(TestDataFactory.createConcert())
        val concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 10, 9))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        val occupation = occupationRepository.save(TestDataFactory.createReleasedOccupation(userId, concertEvent, seats[0], seats[1]))
        reservation = reservationRepository.save(TestDataFactory.createPendingReservation(concertEvent, occupation))
    }

    @Test
    fun `should return success PaymentResult after pay with transaction`() {
        // given: 충분한 돈이 있는 지갑
        val originalWallet =
            walletRepository.save(
                TestDataFactory.createWallet(userId = queueEntry.userId, balance = BigDecimal(2000000)),
            )

        // when
        val result = payUseCase.execute(queueEntry.userId, queueEntry.id, reservation.id)

        // then: 결제 결과에 대한 정보가 있다.
        Assertions.assertThat(result.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(result.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(result.status).isEqualTo(PaymentStatus.COMPLETED)
        Assertions.assertThat(result.failureReason).isNull()

        // then: wallet balance 가 줄어 있다.
        val wallet = walletRepository.findOneByUserId(queueEntry.userId)
        Assertions.assertThat(wallet).isNotNull()
        Assertions.assertThat(wallet!!.balance).isEqualByComparingTo(originalWallet.balance - reservation.totalAmount)

        // then: queueEntry 가 대기열에서 나갔다.
        val entry = queueEntryRepository.findOneByToken(queueEntry.token)
        Assertions.assertThat(entry).isNotNull()
        Assertions.assertThat(entry!!.status).isEqualTo(QueueEntryStatus.EXITED)

        // then: transaction 이 한 개있다.
        val transactions = transactionRepository.findAllByWalletId(wallet.id)
        Assertions.assertThat(transactions).hasSize(1)
        Assertions.assertThat(transactions.first().type).isEqualTo(TransactionType.PAYMENT)
    }

    @Test
    fun `should return fail PaymentResult after pay without transaction`() {
        // given: 돈이 없는 지갑
        val originalWallet =
            walletRepository.save(
                TestDataFactory.createWallet(userId = queueEntry.userId, balance = BigDecimal(200)),
            )

        // when
        val result = payUseCase.execute(queueEntry.userId, queueEntry.id, reservation.id)

        // then: 결제 결과에 실피에 대한 정보가 있다.
        Assertions.assertThat(result.reservationId).isEqualTo(reservation.id)
        Assertions.assertThat(result.amount).isEqualTo(reservation.totalAmount)
        Assertions.assertThat(result.status).isEqualTo(PaymentStatus.FAILED)
        Assertions.assertThat(result.failureReason).isEqualTo("잔액이 부족합니다.")

        // then: wallet balance 가 그대로이다.
        val wallet = walletRepository.findOneByUserId(queueEntry.userId)
        Assertions.assertThat(wallet).isNotNull()
        Assertions.assertThat(wallet!!.balance).isEqualByComparingTo(originalWallet.balance)

        // then: queueEntry 가 대기열에서 나갔다.
        val entry = queueEntryRepository.findOneByToken(queueEntry.token)
        Assertions.assertThat(entry).isNotNull()
        Assertions.assertThat(entry!!.status).isEqualTo(QueueEntryStatus.EXITED)

        // then: transaction 이 없다.
        val transactions = transactionRepository.findAllByWalletId(wallet.id)
        Assertions.assertThat(transactions).isEmpty()
    }
}
