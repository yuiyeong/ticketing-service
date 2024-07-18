package com.yuiyeong.ticketing.integration.domain.service

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import com.yuiyeong.ticketing.domain.service.SeatService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
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
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class SeatServiceTest {
    @Autowired
    private lateinit var seatService: SeatService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @AfterEach
    fun afterEach() {
        seatRepository.deleteAll()
    }

    @Nested
    inner class GetAvailableSeatsTest {
        @Test
        fun `should return all available seats for a concert event`() {
            // given
            val concertEventId = 16L
            val availableSeatIds =
                seatRepository
                    .saveAll(
                        listOf(
                            TestDataFactory.createSeat(concertEventId, isAvailable = true),
                            TestDataFactory.createSeat(concertEventId, isAvailable = true),
                            TestDataFactory.createSeat(concertEventId, isAvailable = false),
                        ),
                    ).filter { it.isAvailable }
                    .map { it.id }

            // when
            val result = seatService.getAvailableSeats(concertEventId)

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result.map { it.id }).containsExactlyInAnyOrderElementsOf(availableSeatIds)
        }

        @Test
        fun `should return empty list when no seats are available`() {
            // given
            val concertEventId = 154L
            seatRepository.saveAll(
                listOf(
                    TestDataFactory.createSeat(concertEventId, isAvailable = false),
                    TestDataFactory.createSeat(concertEventId, isAvailable = false),
                ),
            )

            // when
            val result = seatService.getAvailableSeats(concertEventId)

            // then
            Assertions.assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class OccupyTest {
        @Test
        fun `should successfully occupy a single seat`() {
            // given
            val seat = seatRepository.save(TestDataFactory.createSeat(isAvailable = true))

            // when
            val result = seatService.occupy(listOf(seat.id))

            // then
            Assertions.assertThat(result).hasSize(1)
            Assertions.assertThat(result[0].isAvailable).isFalse()
        }

        @Test
        fun `should successfully occupy available seats`() {
            // given
            val seatIds =
                seatRepository
                    .saveAll(
                        listOf(
                            TestDataFactory.createSeat(isAvailable = true),
                            TestDataFactory.createSeat(isAvailable = true),
                        ),
                    ).map { it.id }

            // when
            val result = seatService.occupy(seatIds)

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result.all { !it.isAvailable }).isTrue()
        }

        @Test
        fun `should throw SeatUnavailableException when any seat is unavailable`() {
            // given
            val seatIds =
                seatRepository
                    .saveAll(
                        listOf(
                            TestDataFactory.createSeat(isAvailable = true),
                            TestDataFactory.createSeat(isAvailable = false),
                        ),
                    ).map { it.id }

            // when & then
            Assertions
                .assertThatThrownBy { seatService.occupy(seatIds) }
                .isInstanceOf(SeatUnavailableException::class.java)
        }

        @Test
        fun `should throw SeatUnavailableException when any seat id does not exist`() {
            // given
            val seat = seatRepository.save(TestDataFactory.createSeat(isAvailable = true))

            // when & then
            Assertions
                .assertThatThrownBy { seatService.occupy(listOf(seat.id, 999L)) }
                .isInstanceOf(SeatUnavailableException::class.java)
        }

        @Test
        fun `should throw SeatUnavailableException when trying to occupy an empty list of seats`() {
            // when & then
            Assertions
                .assertThatThrownBy { seatService.occupy(emptyList()) }
                .isInstanceOf(SeatUnavailableException::class.java)
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("seat_service_test_db")
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
