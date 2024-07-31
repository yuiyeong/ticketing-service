package com.yuiyeong.ticketing.helper

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.domain.model.payment.Payment
import com.yuiyeong.ticketing.domain.model.payment.PaymentMethod
import com.yuiyeong.ticketing.domain.model.payment.PaymentStatus
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.model.wallet.TransactionType
import com.yuiyeong.ticketing.domain.model.wallet.Wallet
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import java.math.BigDecimal
import java.time.ZonedDateTime

object TestDataFactory {
    fun createUnavailableEvent(concert: Concert): ConcertEvent {
        val now = ZonedDateTime.now().asUtc
        val future = now.plusWeeks(1)
        return createConcertEvent(
            concert,
            future,
            future.plusWeeks(1),
            2,
            2,
        )
    }

    fun createAvailableEvent(
        concert: Concert,
        maxSeatCount: Int = 2,
        availableSeatCount: Int = 1,
    ): ConcertEvent {
        val now = ZonedDateTime.now().asUtc
        val past = now.minusWeeks(1)
        return createConcertEvent(
            concert,
            past,
            past.plusWeeks(3),
            maxSeatCount,
            availableSeatCount,
        )
    }

    fun createSeatsOfConcertEvent(concertEvent: ConcertEvent): List<Seat> =
        (0..<concertEvent.maxSeatCount).map {
            val isAvailable = it < concertEvent.availableSeatCount
            createSeat(concertEvent.id, seatNumber = "A$it", isAvailable = isAvailable)
        }

    fun creatActiveOccupationsWithPastExpiresAt(
        concertEvent: ConcertEvent,
        seats: List<Seat>,
    ): List<Occupation> {
        val now = ZonedDateTime.now().asUtc
        val delta = (seats.count() * 5).toLong()
        return creatOccupations(concertEvent, seats, OccupationStatus.ACTIVE, now.minusMinutes(delta))
    }

    fun creatActiveOccupationsWithFutureExpiresAt(
        concertEvent: ConcertEvent,
        seats: List<Seat>,
    ): List<Occupation> {
        val now = ZonedDateTime.now().asUtc
        val delta = (seats.count() * 5).toLong()
        return creatOccupations(concertEvent, seats, OccupationStatus.ACTIVE, now.plusMinutes(delta))
    }

    fun creatOccupations(
        concertEvent: ConcertEvent,
        seats: List<Seat>,
        status: OccupationStatus,
        baseCreatedAt: ZonedDateTime,
    ): List<Occupation> =
        (0..<seats.count()).map {
            val seat = seats[it]
            val longIdx = it.toLong()
            val createdAt = baseCreatedAt.plusSeconds(longIdx)
            val expiresAt = baseCreatedAt.plusMinutes(5).plusSeconds(longIdx)
            val allocation =
                createSeatAllocation(
                    seatId = seat.id,
                    userId = longIdx,
                    seatPrice = seat.price,
                    seatNumber = seat.seatNumber,
                    occupiedAt = createdAt,
                )
            createOccupation(
                userId = longIdx,
                concertEventId = concertEvent.id,
                status = status,
                allocations = listOf(allocation),
                createdAt = createdAt,
                expiresAt = expiresAt,
            )
        }

    fun createReleasedOccupation(
        userId: Long,
        concertEvent: ConcertEvent,
        vararg seats: Seat,
    ): Occupation {
        val pastExpiresAt = ZonedDateTime.now().asUtc.minusHours(1)
        return createOccupation(userId, concertEvent, OccupationStatus.RELEASED, pastExpiresAt, *seats)
    }

    fun createOccupation(
        userId: Long,
        concertEvent: ConcertEvent,
        status: OccupationStatus,
        createdAt: ZonedDateTime,
        vararg seats: Seat,
    ): Occupation {
        val allocations =
            (0..<seats.count()).map {
                createSeatAllocation(
                    seatId = seats[it].id,
                    userId = userId,
                    seatPrice = seats[it].price,
                    seatNumber = seats[it].seatNumber,
                    occupiedAt = createdAt,
                )
            }
        return createOccupation(
            userId = userId,
            concertEventId = concertEvent.id,
            status = status,
            allocations = allocations,
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(5),
        )
    }

    fun createPendingReservation(
        concertEvent: ConcertEvent,
        occupation: Occupation,
    ): Reservation =
        createReservation(
            userId = occupation.userId,
            concertId = concertEvent.concert.id,
            concertEventId = concertEvent.id,
            status = ReservationStatus.PENDING,
            totalSeats = occupation.totalSeats,
            totalAmount = occupation.totalAmount,
        )

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
}
