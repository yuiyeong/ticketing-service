package com.yuiyeong.ticketing.integration.domain.service

import com.yuiyeong.ticketing.TestDataFactory.createConcert
import com.yuiyeong.ticketing.TestDataFactory.createConcertEvent
import com.yuiyeong.ticketing.TestDataFactory.createSeat
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.Concert
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
@Testcontainers
@Execution(ExecutionMode.CONCURRENT)
class ConcertEventServiceTest {
    @Autowired
    private lateinit var concertEventService: ConcertEventService

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    private lateinit var concert: Concert

    @BeforeEach
    fun beforeEach() {
        concert = concertRepository.save(createConcert())
    }

    @AfterEach
    fun afterEach() {
        seatRepository.deleteAll()
        concertEventRepository.deleteAll()
        concertRepository.deleteAll()
    }

    @Nested
    inner class GetAvailableEventsTest {
        @Test
        fun `should return events within reservation period`() {
            // given
            val now = ZonedDateTime.now().asUtc
            val events =
                concertEventRepository.saveAll(
                    listOf(
                        createConcertEvent(concert, now.minusDays(2), now.minusDays(1)),
                        createConcertEvent(concert, now.minusHours(1), now.plusHours(1)),
                        createConcertEvent(concert, now.plusDays(1), now.plusDays(2)),
                    ),
                )
            val currentEvent = events[1]

            // when
            val availableEvents = concertEventService.getAvailableEvents(concert.id)

            // then
            Assertions.assertThat(availableEvents).hasSize(1)
            Assertions.assertThat(availableEvents[0].id).isEqualTo(currentEvent.id)
        }

        @Test
        fun `should return empty list when no events are available`() {
            // given
            val now = ZonedDateTime.now().asUtc
            val pastEvent = createConcertEvent(concert, now.minusDays(2), now.minusDays(1))
            val futureEvent = createConcertEvent(concert, now.plusDays(1), now.plusDays(2))
            concertEventRepository.saveAll(listOf(pastEvent, futureEvent))

            // when
            val availableEvents = concertEventService.getAvailableEvents(concert.id)

            // then
            Assertions.assertThat(availableEvents).isEmpty()
        }

        @Test
        fun `should return events starting exactly now`() {
            // given
            val now = ZonedDateTime.now().asUtc
            val event = concertEventRepository.save(createConcertEvent(concert, now, now.plusHours(1)))

            // when
            val availableEvents = concertEventService.getAvailableEvents(concert.id)

            // then
            Assertions.assertThat(availableEvents).hasSize(1)
            Assertions.assertThat(availableEvents[0].id).isEqualTo(event.id)
        }

        @Test
        fun `should handle events ending exactly now`() {
            // given
            val now = ZonedDateTime.now().asUtc
            val events =
                concertEventRepository.saveAll(
                    listOf(
                        createConcertEvent(concert, now.minusHours(1), now),
                        createConcertEvent(concert, now, now.plusHours(1)),
                    ),
                )
            val futureEvent = events[1]

            // when
            val availableEvents = concertEventService.getAvailableEvents(concert.id)

            // then
            Assertions.assertThat(availableEvents).hasSize(1)
            Assertions.assertThat(availableEvents[0].id).isEqualTo(futureEvent.id)
        }
    }

    @Nested
    inner class GetConcertEventTest {
        @Test
        fun `should return event when it exists`() {
            // given
            val start = ZonedDateTime.now().asUtc
            val concertEvent = createConcertEvent(concert, start, start.plusHours(2))
            val savedEvent = concertEventRepository.save(concertEvent)

            // when
            val result = concertEventService.getConcertEvent(savedEvent.id)

            // then
            Assertions.assertThat(result).isNotNull
            Assertions.assertThat(result.id).isEqualTo(savedEvent.id)
        }

        @Test
        fun `should throw exception when event does not exist`() {
            // given
            val nonExistentId = 999L

            // when & then
            Assertions
                .assertThatThrownBy { concertEventService.getConcertEvent(nonExistentId) }
                .isInstanceOf(ConcertEventNotFoundException::class.java)
        }
    }

    @Nested
    inner class RefreshAvailableSeatsTest {
        private lateinit var concertEvent: ConcertEvent

        @BeforeEach
        fun beforeEach() {
            val now = ZonedDateTime.now().asUtc
            concertEvent = concertEventRepository.save(createConcertEvent(concert, now, now.plusHours(2)))
        }

        @Test
        fun `should update available seat count`() {
            // given
            val availableSeats =
                listOf(
                    createSeat(concertEvent.id, "A1", isAvailable = true),
                    createSeat(concertEvent.id, "A2", isAvailable = true),
                    createSeat(concertEvent.id, "A3", isAvailable = false),
                )
            seatRepository.saveAll(availableSeats)

            // when
            concertEventService.refreshAvailableSeats(concertEvent.id)

            // then
            val updatedEvent = concertEventRepository.findOneById(concertEvent.id)
            Assertions.assertThat(updatedEvent).isNotNull
            Assertions.assertThat(updatedEvent?.availableSeatCount).isEqualTo(2)
        }

        @Test
        fun `should handle case with no available seats`() {
            // given
            val unavailableSeats =
                listOf(
                    createSeat(concertEvent.id, "A1", isAvailable = false),
                    createSeat(concertEvent.id, "A2", isAvailable = false),
                    createSeat(concertEvent.id, "A3", isAvailable = false),
                )
            seatRepository.saveAll(unavailableSeats)

            // when
            concertEventService.refreshAvailableSeats(concertEvent.id)

            // then
            val updatedEvent = concertEventRepository.findOneById(concertEvent.id)
            Assertions.assertThat(updatedEvent).isNotNull
            Assertions.assertThat(updatedEvent?.availableSeatCount).isEqualTo(0)
        }

        @Test
        fun `should throw exception when event does not exist`() {
            // given
            val nonExistentId = 999L

            // when & then
            Assertions
                .assertThatThrownBy { concertEventService.refreshAvailableSeats(nonExistentId) }
                .isInstanceOf(ConcertEventNotFoundException::class.java)
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("concert_event_service_test_db")
                withUsername("testuser")
                withPassword("testpass")
                withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--default-time-zone=+00:00",
                )
                withEnv("TZ", "UTC")
                withReuse(true)
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                "${mysqlContainer.jdbcUrl}?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
            }
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
        }
    }
}
