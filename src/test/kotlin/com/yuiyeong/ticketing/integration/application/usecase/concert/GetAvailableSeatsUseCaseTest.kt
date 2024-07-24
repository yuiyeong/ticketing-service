package com.yuiyeong.ticketing.integration.application.usecase.concert

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableSeatsUseCase
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@Transactional
@SpringBootTest
class GetAvailableSeatsUseCaseTest {
    @Autowired
    private lateinit var getAvailableSeatsUseCase: GetAvailableSeatsUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    private lateinit var unavailableConcertEvent: ConcertEvent
    private lateinit var availableConcertEvent: ConcertEvent
    private lateinit var seats: List<Seat>

    @BeforeEach
    fun setUp() {
        val concert = concertRepository.save(TestDataFactory.createConcert())
        unavailableConcertEvent = concertEventRepository.save(TestDataFactory.createUnavailableEvent(concert))
        seatRepository.saveAll(
            listOf(
                TestDataFactory.createSeat(unavailableConcertEvent.id, isAvailable = true),
                TestDataFactory.createSeat(unavailableConcertEvent.id, isAvailable = true),
            ),
        )
        availableConcertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert))
        seats =
            seatRepository.saveAll(
                listOf(
                    TestDataFactory.createSeat(availableConcertEvent.id, isAvailable = true),
                    TestDataFactory.createSeat(availableConcertEvent.id, isAvailable = false),
                ),
            )
    }

    @Test
    fun `should return only available seats of a concert event that has concertEventId`() {
        // when
        val result = getAvailableSeatsUseCase.execute(availableConcertEvent.id)

        // then
        Assertions.assertThat(result.count()).isEqualTo(1)
        Assertions.assertThat(result[0].id).isEqualTo(seats[0].id)
    }

    @Test
    fun `should throw ReservationNotOpenedException when trying to getAvailableSeatsUseCase with unavailable concertEventId`() {
        // when & then
        Assertions
            .assertThatThrownBy { getAvailableSeatsUseCase.execute(unavailableConcertEvent.id) }
            .isInstanceOf(ReservationNotOpenedException::class.java)
    }

    @Test
    fun `should throw ConcertEventNotFoundException when trying to getAvailableSeatsUseCase with unknown concertEventId`() {
        // given
        val unknownConcertEventId = 8382L

        // when & then
        Assertions
            .assertThatThrownBy { getAvailableSeatsUseCase.execute(unknownConcertEventId) }
            .isInstanceOf(ConcertEventNotFoundException::class.java)
    }
}
