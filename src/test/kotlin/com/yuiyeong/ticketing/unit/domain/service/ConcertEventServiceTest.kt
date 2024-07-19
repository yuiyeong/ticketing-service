package com.yuiyeong.ticketing.unit.domain.service

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.Concert
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import com.yuiyeong.ticketing.domain.service.ConcertEventService
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
class ConcertEventServiceTest {
    @Mock
    private lateinit var concertEventRepository: ConcertEventRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    private lateinit var concertEventService: ConcertEventService

    @BeforeEach
    fun beforeEach() {
        concertEventService = ConcertEventService(concertEventRepository, seatRepository)
    }

    @Test
    fun `should throw ConcertEventNotFoundException when trying with unknown concertEventId`() {
        // given
        val unknownId = 12L
        given(concertEventRepository.findOneById(unknownId)).willReturn(null)

        // when & then
        Assertions
            .assertThatThrownBy { concertEventService.getConcertEvent(unknownId) }
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
        concertEventService.refreshAvailableSeats(concertEvent.id)

        // then
        Assertions.assertThat(concertEvent.availableSeatCount).isNotEqualTo(originalAvailableSeatCount)
        Assertions.assertThat(concertEvent.availableSeatCount).isEqualTo(0)

        verify(concertEventRepository).findOneByIdWithLock(concertEvent.id)
        verify(seatRepository).findAllAvailableByConcertEventId(concertEvent.id)
        verify(concertEventRepository).save(argThat { it -> it.id == concertEvent.id })
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
