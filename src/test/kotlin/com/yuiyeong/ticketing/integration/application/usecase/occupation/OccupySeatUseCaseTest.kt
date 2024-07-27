package com.yuiyeong.ticketing.integration.application.usecase.occupation

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.occupation.OccupySeatUseCase
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
class OccupySeatUseCaseTest {
    @Autowired
    private lateinit var occupySeatUseCase: OccupySeatUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    private lateinit var unavailableConcertEvent: ConcertEvent
    private lateinit var availableConcertEvent: ConcertEvent
    private lateinit var availableSeat: Seat
    private lateinit var unavailableSeat: Seat

    @BeforeEach
    fun setUp() {
        val concert = concertRepository.save(TestDataFactory.createConcert())

        val now = ZonedDateTime.now().asUtc
        val future = now.plusHours(1)
        unavailableConcertEvent = concertEventRepository.save(TestDataFactory.createConcertEvent(concert, future, future.plusHours(5)))

        val past = now.minusHours(1)
        val concertEvent = TestDataFactory.createConcertEvent(concert, past, past.plusHours(5), 2, 1)
        availableConcertEvent = concertEventRepository.save(concertEvent)

        availableSeat = seatRepository.save(TestDataFactory.createSeat(availableConcertEvent.id, isAvailable = true))
        unavailableSeat = seatRepository.save(TestDataFactory.createSeat(availableConcertEvent.id, isAvailable = false))
    }

    @Test
    fun `should return OccupationResult after occupying a seat`() {
        // given
        val userId = 92L

        // when
        val result = occupySeatUseCase.execute(userId, availableConcertEvent.id, availableSeat.id)

        // then
        val occupation = occupationRepository.findOneById(result.id)
        Assertions.assertThat(result.id).isEqualTo(occupation!!.id)
        Assertions.assertThat(result.userId).isEqualTo(occupation.userId)
        Assertions.assertThat(result.status).isEqualTo(occupation.status)
        Assertions.assertThat(result.expiresAt).isEqualTo(occupation.expiresAt)
    }

    @Test
    fun `should throw ReservationNotOpenedException when trying to occupy a seat in a concert event that is not opened`() {
        // given
        val userId = 3L
        val someSeatId = 22L

        // when & then
        Assertions
            .assertThatThrownBy { occupySeatUseCase.execute(userId, unavailableConcertEvent.id, someSeatId) }
            .isInstanceOf(ReservationNotOpenedException::class.java)
    }

    @Test
    fun `should throw SeatUnavailableException when trying to occupy an unavailable seat`() {
        // given
        val userId = 2L

        // when & then
        Assertions
            .assertThatThrownBy { occupySeatUseCase.execute(userId, availableConcertEvent.id, unavailableSeat.id) }
            .isInstanceOf(SeatUnavailableException::class.java)
    }
}
