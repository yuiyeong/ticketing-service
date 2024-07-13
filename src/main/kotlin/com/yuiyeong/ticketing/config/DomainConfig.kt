package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WaitingEntryRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.PaymentService
import com.yuiyeong.ticketing.domain.service.QueueService
import com.yuiyeong.ticketing.domain.service.ReservationService
import com.yuiyeong.ticketing.domain.service.TransactionService
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {
    @Bean
    fun concertEventService(concertEventRepository: ConcertEventRepository) = ConcertEventService(concertEventRepository)

    @Bean
    fun occupationService(
        concertEventRepository: ConcertEventRepository,
        seatRepository: SeatRepository,
        occupationRepository: OccupationRepository,
    ) = OccupationService(concertEventRepository, seatRepository, occupationRepository)

    @Bean
    fun paymentService(
        walletRepository: WalletRepository,
        transactionRepository: TransactionRepository,
        paymentRepository: PaymentRepository,
        reservationRepository: ReservationRepository,
    ) = PaymentService(walletRepository, transactionRepository, paymentRepository, reservationRepository)

    @Bean
    fun queueService(entryRepository: WaitingEntryRepository) = QueueService(entryRepository)

    @Bean
    fun reservationService(
        reservationRepository: ReservationRepository,
        occupationRepository: OccupationRepository,
    ) = ReservationService(reservationRepository, occupationRepository)

    @Bean
    fun transactionService(transactionRepository: TransactionRepository) = TransactionService(transactionRepository)

    @Bean
    fun walletService(walletRepository: WalletRepository) = WalletService(walletRepository)
}
