package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.exception.SeatNotFoundException
import com.yuiyeong.ticketing.domain.model.Concert
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.model.ReservationStatus
import com.yuiyeong.ticketing.domain.model.Seat
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
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
class ReservationServiceTest {
    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var concertEventRepository: ConcertEventRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    private lateinit var reservationService: ReservationService

    @BeforeEach
    fun beforeEach() {
        reservationService = ReservationService(reservationRepository, concertEventRepository, seatRepository)
    }

    @Test
    fun `should return reservation after reserve occupied seats of concert`() {
        // given
        val userId = 12L
        val concertEventId = 22L
        val reservationPeriodStart = ZonedDateTime.now().minusDays(2)
        val concertEvent = createConcertEvent(2L, concertEventId, reservationPeriodStart)
        val occupiedSeats = listOf(createSeat(22L, concertEventId, false), createSeat(23L, concertEventId, false))
        val occupiedSeatIds = occupiedSeats.map { it.id }

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)
        given(seatRepository.findAllByIds(occupiedSeatIds)).willReturn(occupiedSeats)
        given(reservationRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Reservation>(0)
            savedOne.copy(id = 1L)
        }

        // when
        val reservation = reservationService.reserve(userId, concertEventId, occupiedSeatIds)

        // then
        Assertions.assertThat(reservation.userId).isEqualTo(userId)
        Assertions.assertThat(reservation.concertEventId).isEqualTo(concertEventId)
        Assertions.assertThat(reservation.seatIds).isEqualTo(occupiedSeatIds)
        Assertions.assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
        Assertions.assertThat(reservation.totalSeats).isEqualTo(occupiedSeatIds.count())
        Assertions.assertThat(reservation.totalAmount).isEqualTo(occupiedSeats.sumOf { it.price })

        verify(concertEventRepository).findOneById(concertEventId)
        verify(seatRepository).findAllByIds(occupiedSeatIds)
        verify(reservationRepository).save(
            argThat { it ->
                it.userId == userId && it.concertEventId == concertEventId && it.seatIds == occupiedSeatIds
            },
        )
    }

    @Test
    fun `should throw ReservationNotOpenedException when trying to reserve outside the reservation period`() {
        // given
        val userId = 12L
        val concertEventId = 42L
        val reservationPeriodStart = ZonedDateTime.now().plusHours(1)
        val concertEvent = createConcertEvent(1L, concertEventId, reservationPeriodStart)

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)

        // when & then
        Assertions
            .assertThatThrownBy { reservationService.reserve(userId, concertEventId, listOf(1L)) }
            .isInstanceOf(ReservationNotOpenedException::class.java)

        verify(concertEventRepository).findOneById(concertEventId)
    }

    @Test
    fun `should throw SeatNotFoundException when trying to reserve with ids of unknown seats`() {
        // given
        val userId = 9L
        val concertEventId = 8L
        val reservationPeriodStart = ZonedDateTime.now().minusDays(1)
        val concertEvent = createConcertEvent(3L, concertEventId, reservationPeriodStart)
        val occupiedSeat = createSeat(4L, concertEventId, false)
        val occupiedSeatIds = listOf(occupiedSeat.id, 98L)

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)
        given(seatRepository.findAllByIds(occupiedSeatIds)).willReturn(emptyList())

        // when
        Assertions
            .assertThatThrownBy { reservationService.reserve(userId, concertEventId, occupiedSeatIds) }
            .isInstanceOf(SeatNotFoundException::class.java)

        // then
        verify(concertEventRepository).findOneById(concertEventId)
        verify(seatRepository).findAllByIds(occupiedSeatIds)
    }

    @Test
    fun `should confirm a reservation of which status is PENDING`() {
        // given
        val reservationId = 19L
        val reservation = createReservation(reservationId, ReservationStatus.PENDING)
        given(reservationRepository.findOneById(reservationId)).willReturn(reservation)
        given(reservationRepository.save(any())).willAnswer { invocation ->
            invocation.getArgument<Reservation>(0)
        }

        // when
        val confirmedOne = reservationService.confirm(reservationId)

        // then
        Assertions.assertThat(confirmedOne.id).isEqualTo(reservationId)
        Assertions.assertThat(confirmedOne.status).isEqualTo(ReservationStatus.CONFIRMED)

        verify(reservationRepository).findOneById(reservationId)
        verify(reservationRepository).save(argThat { it -> it.id == reservationId })
    }

    @Test
    fun `should throw ReservationNotFoundException when trying to confirm for unknown reservation`() {
        // given
        val unknownReservationId = 19L
        given(reservationRepository.findOneById(unknownReservationId)).willReturn(null)

        // when & then
        Assertions
            .assertThatThrownBy { reservationService.confirm(unknownReservationId) }
            .isInstanceOf(ReservationNotFoundException::class.java)

        verify(reservationRepository).findOneById(unknownReservationId)
    }

    private fun createConcertEvent(
        concertId: Long,
        concertEventId: Long,
        reservationPeriodStart: ZonedDateTime,
    ): ConcertEvent {
        val concert = Concert(concertId, "test title", "test singer", "test description")
        return ConcertEvent(
            id = concertEventId,
            concert = concert,
            venue = "test place",
            reservationPeriod = DateTimeRange(reservationPeriodStart, reservationPeriodStart.plusDays(5)),
            performanceSchedule = DateTimeRange(ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)),
            maxSeatCount = 10,
            availableSeatCount = 4,
        )
    }

    private fun createSeat(
        id: Long,
        concertEventId: Long,
        isAvailable: Boolean,
    ): Seat =
        Seat(
            id = id,
            concertEventId = concertEventId,
            seatNumber = "54번",
            price = BigDecimal(43000),
            isAvailable = isAvailable,
        )

    private fun createReservation(
        reservationId: Long,
        status: ReservationStatus,
    ): Reservation =
        Reservation(
            id = reservationId,
            userId = 12L,
            concertId = 2L,
            concertEventId = 3L,
            status = status,
            seatIds = listOf(11L),
            totalSeats = 1,
            totalAmount = BigDecimal(20000),
            createdAt = ZonedDateTime.now(),
        )
}
