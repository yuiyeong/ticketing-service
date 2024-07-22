package com.yuiyeong.ticketing.integration.domain.service

import com.yuiyeong.ticketing.TestDataFactory.createSeat
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
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
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class OccupationServiceTest {
    @Autowired
    private lateinit var occupationService: OccupationService

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @AfterEach
    fun afterEach() {
        occupationRepository.deleteAll()
        seatRepository.deleteAll()
    }

    @Nested
    inner class CreateOccupationTest {
        @Test
        fun `should create occupation successfully`() {
            // given
            val userId = 1L
            val seats =
                listOf(
                    createSeat(isAvailable = true),
                    createSeat(isAvailable = true),
                )
            val savedSeats = seatRepository.saveAll(seats)
            val seatIds = savedSeats.map { it.id }

            // when
            val result = occupationService.createOccupation(userId, 5L, seatIds)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.allocations).hasSize(2)
            Assertions.assertThat(result.status).isEqualTo(OccupationStatus.ACTIVE)
            Assertions.assertThat(result.expiresAt).isAfter(ZonedDateTime.now().asUtc)
        }

        @Test
        fun `should throw SeatUnavailableException when seats are not available`() {
            // given
            val userId = 1L
            val seats =
                listOf(
                    createSeat(isAvailable = false),
                    createSeat(isAvailable = true),
                )
            val savedSeats = seatRepository.saveAll(seats)
            val seatIds = savedSeats.map { it.id }

            // when & then
            Assertions
                .assertThatThrownBy { occupationService.createOccupation(userId, 12L, seatIds) }
                .isInstanceOf(SeatUnavailableException::class.java)
        }
    }

    @Nested
    inner class ReleaseTest {
        @Test
        fun `should release occupation successfully`() {
            // given
            val userId = 1L
            val seats = listOf(createSeat(isAvailable = true))
            val savedSeats = seatRepository.saveAll(seats)
            val occupation = occupationService.createOccupation(userId, 2L, savedSeats.map { it.id })

            // when
            val result = occupationService.release(userId, occupation.id)

            // then
            Assertions.assertThat(result.status).isEqualTo(OccupationStatus.RELEASED)
            Assertions.assertThat(result.allocations).allMatch { it.status == AllocationStatus.RESERVED }
        }

        @Test
        fun `should throw exception when occupation is already expired`() {
            // given
            val userId = 1L
            val seats = listOf(createSeat(isAvailable = true))
            val savedSeats = seatRepository.saveAll(seats)
            val occupation = occupationService.createOccupation(userId, 12L, savedSeats.map { it.id })

            // Manually expire the occupation
            val expiredOccupation =
                occupation.copy(
                    expiresAt = ZonedDateTime.now().asUtc.minusSeconds(1),
                    status = OccupationStatus.EXPIRED,
                )
            occupationRepository.save(expiredOccupation)

            // when & then
            Assertions
                .assertThatThrownBy { occupationService.release(userId, occupation.id) }
                .isInstanceOf(OccupationAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw exception when occupation is not found`() {
            // given
            val userId = 1L
            val nonExistentOccupationId = 999L

            // when & then
            Assertions
                .assertThatThrownBy { occupationService.release(userId, nonExistentOccupationId) }
                .isInstanceOf(OccupationNotFoundException::class.java)
        }
    }

    @Nested
    inner class ExpireOverdueOccupationsTest {
        @Test
        fun `should expire overdue occupations`() {
            // given
            val userId = 1L
            val seats = listOf(createSeat(isAvailable = true))
            val savedSeats = seatRepository.saveAll(seats)
            val occupation = occupationService.createOccupation(userId, 9L, savedSeats.map { it.id })

            // Manually set occupation to be expired
            val expiredOccupation =
                occupation.copy(
                    expiresAt = ZonedDateTime.now().asUtc.minusSeconds(1),
                )
            occupationRepository.save(expiredOccupation)

            // when
            val result = occupationService.expireOverdueOccupations()

            // then
            Assertions.assertThat(result).hasSize(1)
            Assertions.assertThat(result[0].status).isEqualTo(OccupationStatus.EXPIRED)
            Assertions.assertThat(result[0].expiredAt).isNotNull()
            Assertions.assertThat(result[0].allocations).allMatch { it.status == AllocationStatus.EXPIRED }
        }

        @Test
        fun `should not expire non-overdue occupations`() {
            // given
            val userId = 1L
            val seats = listOf(createSeat(isAvailable = true))
            val savedSeats = seatRepository.saveAll(seats)
            occupationService.createOccupation(userId, 32L, savedSeats.map { it.id })

            // when
            val result = occupationService.expireOverdueOccupations()

            // then
            Assertions.assertThat(result).isEmpty()
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("occupation_service_test_db")
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
