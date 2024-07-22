package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.test.Test

@SpringBootTest
@Transactional
class ConcertEventRepositoryTest {
    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    private lateinit var concert: Concert

    @BeforeEach
    fun beforeEach() {
        concert = concertRepository.save(Concert(0L, "title", "singer", "description"))
    }

    @Test
    fun `should return a concertEvent that has new id after saving it`() {
        // given
        val concertEvent = createConcertEvent(concert.id)

        // when
        val savedOne = concertEventRepository.save(concertEvent)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(concertEvent.id)
        Assertions.assertThat(savedOne.concert.id).isEqualTo(savedOne.concert.id)
        Assertions.assertThat(savedOne.venue).isEqualTo(concertEvent.venue)
        Assertions.assertThat(savedOne.reservationPeriod).isEqualTo(concertEvent.reservationPeriod)
        Assertions.assertThat(savedOne.performanceSchedule).isEqualTo(concertEvent.performanceSchedule)
        Assertions.assertThat(savedOne.maxSeatCount).isEqualByComparingTo(concertEvent.maxSeatCount)
        Assertions.assertThat(savedOne.availableSeatCount).isEqualTo(concertEvent.availableSeatCount)
    }

    @Test
    fun `should return a concertEvent that has the id`() {
        // given
        val concertEvent = createConcertEvent(concert.id)
        val savedOne = concertEventRepository.save(concertEvent)

        // when
        val foundOne = concertEventRepository.findOneById(savedOne.id)

        // then
        Assertions.assertThat(foundOne!!.id).isEqualTo(savedOne.id)
        Assertions.assertThat(foundOne.concert.id).isEqualTo(savedOne.concert.id)
        Assertions.assertThat(foundOne.venue).isEqualTo(savedOne.venue)
        Assertions.assertThat(foundOne.reservationPeriod).isEqualTo(savedOne.reservationPeriod)
        Assertions.assertThat(foundOne.performanceSchedule).isEqualTo(savedOne.performanceSchedule)
        Assertions.assertThat(foundOne.maxSeatCount).isEqualByComparingTo(savedOne.maxSeatCount)
        Assertions.assertThat(foundOne.availableSeatCount).isEqualTo(savedOne.availableSeatCount)
    }

    @Test
    fun `should return concertEvents of which reservationPeriod covers a moment`() {
        // given
        val reservationPeriodStart1 = ZonedDateTime.now().asUtc.minusDays(1)
        val concertEvent = concertEventRepository.save(createConcertEvent(concert.id, reservationPeriodStart1))

        val reservationPeriodStart2 = ZonedDateTime.now().asUtc.plusDays(1)
        concertEventRepository.save(createConcertEvent(concert.id, reservationPeriodStart2))

        // when
        val concertEvents = concertEventRepository.findAllWithinPeriodBy(concert.id, ZonedDateTime.now().asUtc)

        // then
        Assertions.assertThat(concertEvents.count()).isEqualTo(1)
        Assertions.assertThat(concertEvents[0].id).isEqualTo(concertEvent.id)
        Assertions.assertThat(concertEvents[0].concert.id).isEqualTo(concertEvent.concert.id)
        Assertions.assertThat(concertEvents[0].venue).isEqualTo(concertEvent.venue)
        Assertions.assertThat(concertEvents[0].reservationPeriod).isEqualTo(concertEvent.reservationPeriod)
        Assertions.assertThat(concertEvents[0].performanceSchedule).isEqualTo(concertEvent.performanceSchedule)
        Assertions.assertThat(concertEvents[0].maxSeatCount).isEqualTo(concertEvent.maxSeatCount)
        Assertions.assertThat(concertEvents[0].availableSeatCount).isEqualTo(concertEvent.availableSeatCount)
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
