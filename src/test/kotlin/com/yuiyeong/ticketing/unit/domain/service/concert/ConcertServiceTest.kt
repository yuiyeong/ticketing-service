package com.yuiyeong.ticketing.unit.domain.service.concert

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
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
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ConcertServiceTest {
    @Mock
    private lateinit var concertEventRepository: ConcertEventRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    private lateinit var concertService: ConcertService

    @BeforeEach
    fun beforeEach() {
        concertService = ConcertService(concertEventRepository, seatRepository)
    }

    @Test
    fun `should throw ConcertEventNotFoundException when trying with unknown concertEventId`() {
        // given
        val unknownId = 12L
        given(concertEventRepository.findOneById(unknownId)).willReturn(null)

        // when & then
        Assertions
            .assertThatThrownBy { concertService.getConcertEvent(unknownId) }
            .isInstanceOf(ConcertEventNotFoundException::class.java)

        verify(concertEventRepository).findOneById(unknownId)
    }

    @Test
    fun `should refresh availableSeatCount of concertEvent`() {
        // given
        val concertEvent = createConcertEvent(2L)
        val originalAvailableSeatCount = concertEvent.availableSeatCount

        given(concertEventRepository.findOneByIdWithLock(concertEvent.id)).willReturn(concertEvent)
        given(seatRepository.findAllAvailableByConcertEventId(concertEvent.id)).willReturn(emptyList())
        given(concertEventRepository.save(any())).willAnswer { invocation -> invocation.getArgument<ConcertEvent>(0) }

        // when
        val result = concertService.refreshAvailableSeats(concertEvent.id)

        // then
        Assertions.assertThat(result.availableSeatCount).isNotEqualTo(originalAvailableSeatCount)
        Assertions.assertThat(result.availableSeatCount).isEqualTo(0)

        verify(concertEventRepository).findOneByIdWithLock(result.id)
        verify(seatRepository).findAllAvailableByConcertEventId(result.id)
        verify(concertEventRepository).save(argThat { it -> it.id == result.id })
    }

    private fun createConcertEvent(
        concertId: Long,
        reservationPeriodStart: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): ConcertEvent {
        val random = Random(System.currentTimeMillis())
        val concert = Concert(concertId, "title", "singer", "description")
        val scheduleStartAt = reservationPeriodStart.plusMonths(1)
        return ConcertEvent(
            id = 0L,
            concert = concert,
            venue = "venue",
            reservationPeriod = DateTimeRange(reservationPeriodStart, reservationPeriodStart.plusDays(5)),
            performanceSchedule = DateTimeRange(scheduleStartAt, scheduleStartAt.plusHours(1)),
            maxSeatCount = random.nextInt(100),
            availableSeatCount = random.nextInt(60),
        )
    }
}
