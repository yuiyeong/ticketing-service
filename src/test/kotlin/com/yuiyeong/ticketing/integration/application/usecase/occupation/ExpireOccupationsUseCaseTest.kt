package com.yuiyeong.ticketing.integration.application.usecase.occupation

import com.yuiyeong.ticketing.application.usecase.occupation.ExpireOccupationsUseCase
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.helper.TestDataFactory
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.test.Test

@Transactional
@SpringBootTest
class ExpireOccupationsUseCaseTest {
    @Autowired
    private lateinit var expireOccupationsUseCase: ExpireOccupationsUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    @Test
    fun `should expire occupations that has past expiresAt`() {
        // given: 총 10개의 좌석이 있는 concert event 에서 4개가 점유되었고 그 4개가 만료 시간이 지난 상황
        val concert = concertRepository.save(TestDataFactory.createConcert())
        val concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 10, 6))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        val occupiedSeatCount = concertEvent.maxSeatCount - concertEvent.availableSeatCount
        val occupations =
            occupationRepository.saveAll(
                TestDataFactory.creatActiveOccupationsWithPastExpiresAt(concertEvent, seats.subList(0, occupiedSeatCount)),
            )

        // when
        val expiredOccupations = expireOccupationsUseCase.execute()

        // then
        Assertions.assertThat(expiredOccupations.count()).isEqualTo(occupations.count())
        for (expiredOccupation in expiredOccupations) {
            Assertions.assertThat(expiredOccupation.status).isEqualTo(OccupationStatus.EXPIRED)
        }
    }

    @Test
    fun `should expire only occupations that has past expiresAt`() {
        // given: 총 20개의 좌석이 있는 concert event 에서, 5개가 만료 시간이 지난 점유 상태이고, 또 다른 5개는 만료 시간이 지나지 않은 점유 상태
        val concert = concertRepository.save(TestDataFactory.createConcert())
        val concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 20, 10))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        val (seats1, seats2) = seats.partition { it.id > 5 }
        // 만료 시간이 지난 점유
        val occupations = occupationRepository.saveAll(TestDataFactory.creatActiveOccupationsWithPastExpiresAt(concertEvent, seats1))
        // 만료 시간이 지나지 않은 점유
        occupationRepository.saveAll(TestDataFactory.creatActiveOccupationsWithFutureExpiresAt(concertEvent, seats2))

        // when
        val expiredOccupations = expireOccupationsUseCase.execute()

        // then: 만료시간이 지난 5개가 만료처리되어서 반환되어야한다.
        Assertions.assertThat(expiredOccupations.size).isEqualTo(occupations.count())
        expiredOccupations.forEachIndexed { idx, occupation ->
            Assertions.assertThat(occupation.id).isEqualTo(occupations[idx].id)
            Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.EXPIRED)
        }
    }

    @Test
    fun `should expire only active occupations that has past expiresAt`() {
        // given: 총 10개의 좌석이 있는 concert event 에서, 2개가 만료 상태이고, 2개가 예약 상태, 2개가 만료 시간이 지난 점유 상태인 상황
        val concert = concertRepository.save(TestDataFactory.createConcert())
        val concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 10, 4))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        val pastExpiresAt = ZonedDateTime.now().asUtc.minusMinutes(10)
        // 2개가 만료 상태
        occupationRepository.saveAll(
            TestDataFactory.creatOccupations(concertEvent, listOf(seats[0], seats[1]), OccupationStatus.EXPIRED, pastExpiresAt),
        )
        // 2개가 예약 상태
        occupationRepository.saveAll(
            TestDataFactory.creatOccupations(concertEvent, listOf(seats[2], seats[3]), OccupationStatus.RELEASED, pastExpiresAt),
        )
        // 2개가 만료 시간이 지난 점유 상태
        val occupations =
            occupationRepository.saveAll(
                TestDataFactory.creatOccupations(concertEvent, listOf(seats[4], seats[5]), OccupationStatus.ACTIVE, pastExpiresAt),
            )

        // when
        val expiredOccupations = expireOccupationsUseCase.execute()

        // then: 만료 시간이 지난 2개에 대해서만 만료 처리가 되어서 반환되어야한다.
        Assertions.assertThat(expiredOccupations.size).isEqualTo(occupations.count())
        expiredOccupations.forEachIndexed { idx, occupation ->
            Assertions.assertThat(occupation.id).isEqualTo(occupations[idx].id)
            Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.EXPIRED)
        }
    }

    @Test
    fun `should expire no occupations when there is no active occupations`() {
        // given: 총 20개의 좌석이 있는 concert event 에서, 20개가 만료 시간이 지나지 않은 점유 상태
        val concert = concertRepository.save(TestDataFactory.createConcert())
        val concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 20, 0))
        val seats = seatRepository.saveAll(TestDataFactory.createSeatsOfConcertEvent(concertEvent))
        occupationRepository.saveAll(TestDataFactory.creatActiveOccupationsWithFutureExpiresAt(concertEvent, seats))

        // when
        val expiredOccupations = expireOccupationsUseCase.execute()

        // then: 만료 처리된 것이 하나도 없어야 한다.
        Assertions.assertThat(expiredOccupations).isEmpty()
    }
}
