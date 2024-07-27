package com.yuiyeong.ticketing.integration.domain.service.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationAlreadyConfirmedException
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.service.reservation.ReservationService
import com.yuiyeong.ticketing.helper.TestDataFactory.createConcert
import com.yuiyeong.ticketing.helper.TestDataFactory.createConcertEvent
import com.yuiyeong.ticketing.helper.TestDataFactory.createOccupation
import com.yuiyeong.ticketing.helper.TestDataFactory.createReservation
import com.yuiyeong.ticketing.helper.TestDataFactory.createSeat
import com.yuiyeong.ticketing.helper.TestDataFactory.createSeatAllocation
import org.assertj.core.api.Assertions
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
class ReservationServiceTest {
    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Nested
    inner class ReserveTest {
        @Test
        fun `should create reservation successfully`() {
            // given
            val userId = 1L
            val concert = concertRepository.save(createConcert())
            val concertEvent =
                createConcertEvent(
                    concert = concert,
                    reservationStart = ZonedDateTime.now().asUtc.minusHours(1),
                    reservationEnd = ZonedDateTime.now().asUtc.plusHours(1),
                )
            val savedConcertEvent = concertEventRepository.save(concertEvent)

            val seat = createSeat(concertEventId = savedConcertEvent.id)
            val savedSeat = seatRepository.save(seat)

            val seatAllocation = createSeatAllocation(seatId = savedSeat.id, userId = userId)
            val occupation = createOccupation(userId = userId, allocations = listOf(seatAllocation))
            val savedOccupation = occupationRepository.save(occupation)

            // when
            val result = reservationService.reserve(userId, savedConcertEvent.id, savedOccupation.id)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.concertId).isEqualTo(concert.id)
            Assertions.assertThat(result.concertEventId).isEqualTo(savedConcertEvent.id)
            Assertions.assertThat(result.status).isEqualTo(ReservationStatus.PENDING)
            Assertions.assertThat(result.totalSeats).isEqualTo(1)
            Assertions.assertThat(result.totalAmount).isEqualTo(seat.price)
        }

        @Test
        fun `should throw exception when concert event is not found`() {
            // given
            val userId = 1L
            val nonExistentConcertEventId = 999L
            val occupation = createOccupation(userId = userId)
            val savedOccupation = occupationRepository.save(occupation)

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.reserve(userId, nonExistentConcertEventId, savedOccupation.id)
                }.isInstanceOf(ConcertEventNotFoundException::class.java)
        }

        @Test
        fun `should throw exception when reservation period is over`() {
            // given
            val userId = 1L
            val concert = concertRepository.save(createConcert())
            val concertEvent =
                createConcertEvent(
                    concert = concert,
                    reservationStart = ZonedDateTime.now().asUtc.minusHours(2),
                    reservationEnd = ZonedDateTime.now().asUtc.minusHours(1),
                )
            val savedConcertEvent = concertEventRepository.save(concertEvent)

            val occupation = createOccupation(userId = userId)
            val savedOccupation = occupationRepository.save(occupation)

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.reserve(userId, savedConcertEvent.id, savedOccupation.id)
                }.isInstanceOf(ReservationNotOpenedException::class.java)
        }

        @Test
        fun `should throw exception when occupation is not found`() {
            // given
            val userId = 1L
            val concert = concertRepository.save(createConcert())
            val concertEvent =
                createConcertEvent(
                    concert = concert,
                    reservationStart = ZonedDateTime.now().asUtc.minusHours(1),
                    reservationEnd = ZonedDateTime.now().asUtc.plusHours(1),
                )
            val savedConcertEvent = concertEventRepository.save(concertEvent)

            val nonExistentOccupationId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.reserve(userId, savedConcertEvent.id, nonExistentOccupationId)
                }.isInstanceOf(OccupationNotFoundException::class.java)
        }
    }

    @Nested
    inner class GetReservationTest {
        @Test
        fun `should return reservation when it exists`() {
            // given
            val reservation = createReservation()
            val savedReservation = reservationRepository.save(reservation)

            // when
            val result = reservationService.getReservation(savedReservation.id)

            // then
            Assertions.assertThat(result.id).isEqualTo(savedReservation.id)
        }

        @Test
        fun `should throw exception when reservation is not found`() {
            // given
            val nonExistentReservationId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.getReservation(nonExistentReservationId)
                }.isInstanceOf(ReservationNotFoundException::class.java)
        }
    }

    @Nested
    inner class ConfirmTest {
        @Test
        fun `should confirm reservation successfully`() {
            // given
            val reservation = createReservation(status = ReservationStatus.PENDING)
            val savedReservation = reservationRepository.save(reservation)

            // when
            val result = reservationService.confirm(savedReservation.id)

            // then
            Assertions.assertThat(result.status).isEqualTo(ReservationStatus.CONFIRMED)
        }

        @Test
        fun `should throw exception when trying to confirm already confirmed reservation`() {
            // given
            val reservation = createReservation(status = ReservationStatus.CONFIRMED)
            val savedReservation = reservationRepository.save(reservation)

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.confirm(savedReservation.id)
                }.isInstanceOf(ReservationAlreadyConfirmedException::class.java)
        }

        @Test
        fun `should throw exception when reservation is not found`() {
            // given
            val nonExistentReservationId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    reservationService.confirm(nonExistentReservationId)
                }.isInstanceOf(ReservationNotFoundException::class.java)
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("reservation_service_test_db")
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
