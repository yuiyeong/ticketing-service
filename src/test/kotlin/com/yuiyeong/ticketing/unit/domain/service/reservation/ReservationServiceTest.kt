package com.yuiyeong.ticketing.unit.domain.service.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.service.reservation.ReservationService
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
    private lateinit var occupationRepository: OccupationRepository

    private lateinit var reservationService: ReservationService

    @BeforeEach
    fun beforeEach() {
        reservationService = ReservationService(reservationRepository, concertEventRepository, occupationRepository)
    }

    @Test
    fun `should return reservation after reserve occupied seats of concert`() {
        // given
        val userId = 12L
        val concertEventId = 22L
        val reservationPeriodStart = ZonedDateTime.now().asUtc.minusDays(2)
        val concertEvent = createConcertEvent(2L, concertEventId, reservationPeriodStart)
        val occupation = createOccupation(userId, listOf(82L), 12L)

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)
        given(occupationRepository.findOneById(occupation.id)).willReturn(occupation)
        given(reservationRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Reservation>(0)
            savedOne.copy(id = 1L)
        }

        // when
        val reservation = reservationService.reserve(userId, concertEventId, occupation.id)

        // then
        Assertions.assertThat(reservation.userId).isEqualTo(userId)
        Assertions.assertThat(reservation.concertEventId).isEqualTo(concertEventId)
        Assertions.assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
        Assertions.assertThat(reservation.totalSeats).isEqualTo(occupation.allocations.count())
        Assertions.assertThat(reservation.totalAmount).isEqualTo(occupation.allocations.sumOf { it.seatPrice })

        verify(concertEventRepository).findOneById(concertEventId)
        verify(occupationRepository).findOneById(occupation.id)
        verify(reservationRepository).save(argThat { it -> it.userId == userId && it.concertEventId == concertEventId })
    }

    @Test
    fun `should throw ReservationNotOpenedException when trying to reserve outside the reservation period`() {
        // given
        val userId = 12L
        val concertEventId = 42L
        val reservationPeriodStart = ZonedDateTime.now().asUtc.plusHours(1)
        val concertEvent = createConcertEvent(1L, concertEventId, reservationPeriodStart)
        val occupationId = 1L

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)

        // when & then
        Assertions
            .assertThatThrownBy { reservationService.reserve(userId, concertEventId, occupationId) }
            .isInstanceOf(ReservationNotOpenedException::class.java)

        verify(concertEventRepository).findOneById(concertEventId)
    }

    @Test
    fun `should throw SeatNotFoundException when trying to reserve with ids of unknown seats`() {
        // given
        val userId = 9L
        val concertEventId = 8L
        val reservationPeriodStart = ZonedDateTime.now().asUtc.minusDays(1)
        val concertEvent = createConcertEvent(3L, concertEventId, reservationPeriodStart)
        val unknownOccupationId = 123L

        given(concertEventRepository.findOneById(concertEventId)).willReturn(concertEvent)
        given(occupationRepository.findOneById(unknownOccupationId)).willReturn(null)

        // when
        Assertions
            .assertThatThrownBy { reservationService.reserve(userId, concertEventId, unknownOccupationId) }
            .isInstanceOf(OccupationNotFoundException::class.java)

        // then
        verify(concertEventRepository).findOneById(concertEventId)
        verify(occupationRepository).findOneById(unknownOccupationId)
    }

    @Test
    fun `should confirm a reservation of which status is PENDING`() {
        // given
        val reservationId = 19L
        val occupation = createOccupation(5L, listOf(6L), 2L)
        val reservation = createReservation(reservationId, ReservationStatus.PENDING, occupation)
        given(reservationRepository.findOneByIdWithLock(reservationId)).willReturn(reservation)
        given(reservationRepository.save(any())).willAnswer { invocation ->
            invocation.getArgument<Reservation>(0)
        }

        // when
        val confirmedOne = reservationService.confirm(reservationId)

        // then
        Assertions.assertThat(confirmedOne.id).isEqualTo(reservationId)
        Assertions.assertThat(confirmedOne.status).isEqualTo(ReservationStatus.CONFIRMED)

        verify(reservationRepository).findOneByIdWithLock(reservationId)
        verify(reservationRepository).save(argThat { it -> it.id == reservationId })
    }

    @Test
    fun `should throw ReservationNotFoundException when trying to confirm for unknown reservation`() {
        // given
        val unknownReservationId = 19L
        given(reservationRepository.findOneByIdWithLock(unknownReservationId)).willReturn(null)

        // when & then
        Assertions
            .assertThatThrownBy { reservationService.confirm(unknownReservationId) }
            .isInstanceOf(ReservationNotFoundException::class.java)

        verify(reservationRepository).findOneByIdWithLock(unknownReservationId)
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
            performanceSchedule = DateTimeRange(ZonedDateTime.now().asUtc, ZonedDateTime.now().asUtc.plusHours(1)),
            maxSeatCount = 10,
            availableSeatCount = 4,
        )
    }

    private fun createOccupation(
        userId: Long,
        seatIds: List<Long>,
        id: Long = 0L,
        status: OccupationStatus = OccupationStatus.ACTIVE,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Occupation {
        val allocationStatus =
            if (status == OccupationStatus.ACTIVE) AllocationStatus.OCCUPIED else AllocationStatus.valueOf(status.name)
        val allocations =
            seatIds.map {
                SeatAllocation(
                    id = it,
                    userId = userId,
                    seatId = it,
                    seatNumber = "$it 번",
                    seatPrice = BigDecimal(20000),
                    status = allocationStatus,
                    occupiedAt = createdAt,
                    expiredAt = null,
                    reservedAt = null,
                )
            }
        return Occupation(
            id = id,
            userId = userId,
            concertEventId = 389L,
            allocations = allocations,
            status = status,
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(5),
            expiredAt = null,
        )
    }

    private fun createReservation(
        reservationId: Long,
        status: ReservationStatus,
        occupation: Occupation,
    ): Reservation =
        Reservation(
            id = reservationId,
            userId = 12L,
            concertId = 2L,
            concertEventId = 3L,
            status = status,
            totalSeats = occupation.allocations.count(),
            totalAmount = occupation.allocations.sumOf { it.seatPrice },
            createdAt = ZonedDateTime.now().asUtc,
        )
}
