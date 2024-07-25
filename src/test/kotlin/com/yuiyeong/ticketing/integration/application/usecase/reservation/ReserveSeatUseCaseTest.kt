package com.yuiyeong.ticketing.integration.application.usecase.reservation

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.reservation.ReserveSeatUseCase
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import com.yuiyeong.ticketing.domain.exception.OccupationInvalidException
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.test.Test

@Transactional
@SpringBootTest
class ReserveSeatUseCaseTest {
    @Autowired
    private lateinit var reserveSeatUseCase: ReserveSeatUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    private lateinit var concertEvent: ConcertEvent
    private lateinit var expiredOccupation: Occupation
    private lateinit var releasedOccupation: Occupation
    private lateinit var occupationToExpire: Occupation
    private lateinit var activeOccupation: Occupation

    @BeforeEach
    fun setUp() {
        // given: 총 20개의 좌석이 있는 concert event 와 4개의 점유
        val concert = concertRepository.save(TestDataFactory.createConcert())
        concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 20, 14))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        val now = ZonedDateTime.now().asUtc
        val pastExpiresAt = now.minusHours(1)

        val occupations =
            occupationRepository.saveAll(
                listOf(
                    // 만료 상태 점유
                    TestDataFactory.createOccupation(3L, concertEvent, OccupationStatus.EXPIRED, pastExpiresAt, seats[0]),
                    // 예약 상태 점유
                    TestDataFactory.createOccupation(19L, concertEvent, OccupationStatus.RELEASED, pastExpiresAt, seats[1], seats[2]),
                    // 만료 시간이 지난 점유
                    TestDataFactory.createOccupation(17L, concertEvent, OccupationStatus.ACTIVE, pastExpiresAt, seats[3]),
                    // 만료 시간이 지나지 않은 점유
                    TestDataFactory.createOccupation(98L, concertEvent, OccupationStatus.ACTIVE, now, seats[4], seats[5]),
                ),
            )
        expiredOccupation = occupations[0]
        releasedOccupation = occupations[1]
        occupationToExpire = occupations[2]
        activeOccupation = occupations[3]
    }

    @Test
    fun `should return ReservationResult after reserve seat successfully`() {
        // when
        val result = reserveSeatUseCase.execute(activeOccupation.userId, concertEvent.id, activeOccupation.id)

        // then
        Assertions.assertThat(result.userId).isEqualTo(activeOccupation.userId)
        Assertions.assertThat(result.concertVenue).isEqualTo(concertEvent.venue)
        Assertions.assertThat(result.concertPerformanceStartAt).isEqualTo(concertEvent.performanceSchedule.start)
        Assertions.assertThat(result.status).isEqualTo(ReservationStatus.PENDING)
        Assertions.assertThat(result.totalSeats).isEqualTo(activeOccupation.totalSeats)
        Assertions.assertThat(result.totalAmount).isEqualTo(activeOccupation.totalAmount)

        val occupation = occupationRepository.findOneById(activeOccupation.id)!!
        Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.RELEASED)
    }

    @Test
    fun `should throw ConcertEventNotFoundException when trying to reserve a seat for not found concert event`() {
        // given
        val unknownConcertEventId = 123L

        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(activeOccupation.userId, unknownConcertEventId, activeOccupation.id)
            }.isInstanceOf(ConcertEventNotFoundException::class.java)
    }

    @Test
    fun `should throw ReservationNotOpenedException when trying to reserve a seat in a concert event that is not opened`() {
        // given
        val concertEvent = concertEventRepository.save(TestDataFactory.createUnavailableEvent(concertEvent.concert))

        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(activeOccupation.userId, concertEvent.id, activeOccupation.id)
            }.isInstanceOf(ReservationNotOpenedException::class.java)
    }

    @Test
    fun `should throw OccupationNotFoundException when trying to reserve a seat for not found occupation`() {
        // given
        val unknownOccupationId = 9993L

        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(activeOccupation.userId, concertEvent.id, unknownOccupationId)
            }.isInstanceOf(OccupationNotFoundException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyExpiredException when trying to reserve a seat for expired occupation`() {
        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(expiredOccupation.userId, concertEvent.id, expiredOccupation.id)
            }.isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyExpiredException when trying to reserve a seat for occupation to be expired`() {
        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(occupationToExpire.userId, concertEvent.id, occupationToExpire.id)
            }.isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyReleaseException when trying to reserve a seat for invalid occupation`() {
        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(releasedOccupation.userId, concertEvent.id, releasedOccupation.id)
            }.isInstanceOf(OccupationAlreadyReleaseException::class.java)
    }

    @Test
    fun `should throw OccupationInvalidException when trying to reserve a seat of another user`() {
        // given
        val newUserId = 1098L

        // when & then
        Assertions
            .assertThatThrownBy {
                reserveSeatUseCase.execute(newUserId, concertEvent.id, activeOccupation.id)
            }.isInstanceOf(OccupationInvalidException::class.java)
    }
}
