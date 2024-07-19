package com.yuiyeong.ticketing

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.AllocationStatus
import com.yuiyeong.ticketing.domain.model.Concert
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.model.OccupationStatus
import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.PaymentMethod
import com.yuiyeong.ticketing.domain.model.PaymentStatus
import com.yuiyeong.ticketing.domain.model.QueueEntry
import com.yuiyeong.ticketing.domain.model.QueueEntryStatus
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.model.ReservationStatus
import com.yuiyeong.ticketing.domain.model.Seat
import com.yuiyeong.ticketing.domain.model.SeatAllocation
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import java.math.BigDecimal
import java.time.ZonedDateTime

object TestDataFactory {
    fun createConcert(): Concert =
        Concert(
            id = 0L,
            title = "Test Concert",
            singer = "Test Singer",
            description = "Test Description",
        )

    fun createConcertEvent(
        concert: Concert,
        reservationStart: ZonedDateTime,
        reservationEnd: ZonedDateTime,
        maxSeatCount: Int = 50,
        availableSeatCount: Int = 50,
    ): ConcertEvent =
        ConcertEvent(
            id = 0L,
            concert = concert,
            venue = "Test Venue",
            reservationPeriod = DateTimeRange(reservationStart, reservationEnd),
            performanceSchedule = DateTimeRange(reservationEnd, reservationEnd.plusHours(2)),
            maxSeatCount = maxSeatCount,
            availableSeatCount = availableSeatCount,
        )

    fun createSeat(
        concertEventId: Long = 19L,
        seatNumber: String = "A1",
        price: BigDecimal = 10000.toBigDecimal(),
        isAvailable: Boolean = true,
    ): Seat =
        Seat(
            id = 0L,
            concertEventId = concertEventId,
            seatNumber = seatNumber,
            price = price,
            isAvailable = isAvailable,
        )

    fun createOccupation(
        id: Long = 0L,
        userId: Long = 1L,
        concertEventId: Long = 1L,
        allocations: List<SeatAllocation> = listOf(createSeatAllocation()),
        status: OccupationStatus = OccupationStatus.ACTIVE,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
        expiresAt: ZonedDateTime = ZonedDateTime.now().asUtc.plusMinutes(5),
        expiredAt: ZonedDateTime? = null,
    ): Occupation =
        Occupation(
            id = id,
            userId = userId,
            concertEventId = concertEventId,
            allocations = allocations,
            status = status,
            createdAt = createdAt,
            expiresAt = expiresAt,
            expiredAt = expiredAt,
        )

    fun createSeatAllocation(
        id: Long = 0L,
        seatId: Long = 1L,
        userId: Long = 1L,
        seatPrice: BigDecimal = 10000.toBigDecimal(),
        seatNumber: String = "A1",
        status: AllocationStatus = AllocationStatus.OCCUPIED,
        occupiedAt: ZonedDateTime = ZonedDateTime.now().asUtc,
        expiredAt: ZonedDateTime? = null,
        reservedAt: ZonedDateTime? = null,
    ): SeatAllocation =
        SeatAllocation(
            id = id,
            seatId = seatId,
            userId = userId,
            seatPrice = seatPrice,
            seatNumber = seatNumber,
            status = status,
            occupiedAt = occupiedAt,
            expiredAt = expiredAt,
            reservedAt = reservedAt,
        )

    fun createReservation(
        id: Long = 0L,
        userId: Long = 1L,
        concertId: Long = 1L,
        concertEventId: Long = 1L,
        status: ReservationStatus = ReservationStatus.PENDING,
        totalSeats: Int = 1,
        totalAmount: BigDecimal = 20000.toBigDecimal(),
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Reservation =
        Reservation(
            id = id,
            userId = userId,
            concertId = concertId,
            concertEventId = concertEventId,
            status = status,
            totalSeats = totalSeats,
            totalAmount = totalAmount,
            createdAt = createdAt,
        )

    fun createPayment(
        id: Long = 0L,
        userId: Long = 1L,
        transactionId: Long? = null,
        reservationId: Long = 1L,
        amount: BigDecimal = 1000.toBigDecimal(),
        status: PaymentStatus = PaymentStatus.COMPLETED,
        paymentMethod: PaymentMethod =
            PaymentMethod.WALLET,
        failureReason: String? = null,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
        updatedAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Payment =
        Payment(
            id = id,
            userId = userId,
            transactionId = transactionId,
            reservationId = reservationId,
            amount = amount,
            status = status,
            paymentMethod = paymentMethod,
            failureReason = failureReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun createTransaction(
        id: Long = 0L,
        walletId: Long = 1L,
        amount: BigDecimal = 1000.toBigDecimal(),
        type: TransactionType = TransactionType.PAYMENT,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Transaction =
        Transaction(
            id = id,
            walletId = walletId,
            amount = amount,
            type = type,
            createdAt = createdAt,
        )

    fun createWallet(
        id: Long = 0L,
        userId: Long = 1L,
        balance: BigDecimal = BigDecimal.ZERO,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
        updatedAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Wallet =
        Wallet(
            id = id,
            userId = userId,
            balance = balance,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun createQueueEntry(
        id: Long = 0L,
        userId: Long = 1L,
        token: String = "test-token",
        position: Long = 0L,
        status: QueueEntryStatus = QueueEntryStatus.WAITING,
        expiresAt: ZonedDateTime = ZonedDateTime.now().asUtc.plusMinutes(30),
        enteredAt: ZonedDateTime = ZonedDateTime.now().asUtc,
        processingStartedAt: ZonedDateTime? = null,
        exitedAt: ZonedDateTime? = null,
        expiredAt: ZonedDateTime? = null,
    ): QueueEntry =
        QueueEntry(
            id = id,
            userId = userId,
            token = token,
            position = position,
            status = status,
            expiresAt = expiresAt,
            enteredAt = enteredAt,
            processingStartedAt = processingStartedAt,
            exitedAt = exitedAt,
            expiredAt = expiredAt,
        )
}
