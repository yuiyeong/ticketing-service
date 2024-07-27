package com.yuiyeong.ticketing.integration.concurrent

import com.yuiyeong.ticketing.TestDataFactory
import com.yuiyeong.ticketing.application.usecase.occupation.OccupySeatUseCase
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.test.Test

@SpringBootTest
class OccupySeatUseCaseConcurrentTest {
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

    private lateinit var concertEvent: ConcertEvent
    private lateinit var seat: Seat

    private val logger = LoggerFactory.getLogger("좌석 점유 동시성 테스트")

    @BeforeEach
    fun beforeEach() {
        val concert = concertRepository.save(TestDataFactory.createConcert())
        concertEvent = concertEventRepository.save(TestDataFactory.createAvailableEvent(concert, 1, 1))
        seat = seatRepository.save(TestDataFactory.createSeatsOfConcertEvent(concertEvent).first())
    }

    @AfterEach
    fun afterEach() {
        occupationRepository.deleteAll()
        seatRepository.deleteAll()
        concertRepository.deleteAll()
        concertRepository.deleteAll()
    }

    @Test
    fun `should accept only one execution for occupation a seat when multiple requests come simultaneously`() {
        // given
        val userIds = (1..5000).map { it.toLong() }
        // userId 별 성공 여부
        val results = ConcurrentHashMap<Long, Boolean>()
        val maxWorkingTime = AtomicReference(0L)

        val taskCount = userIds.count()
        val latch = CountDownLatch(taskCount)
        val executor = Executors.newFixedThreadPool(taskCount)

        // when: 10 개의 thread 가 동시에 실행. 10명이 동시에 한 좌석을 점유하려고 함
        userIds.forEach { userId ->
            executor.submit {
                val startTime = System.nanoTime()
                try {
                    occupySeatUseCase.execute(userId, concertEvent.id, seat.id)
                    results[userId] = true
                } catch (e: Exception) {
                    results[userId] = false
                } finally {
                    val threadTime = System.nanoTime() - startTime
                    maxWorkingTime.updateAndGet { max(it, threadTime) }
                    latch.countDown()
                }
            }
        }
        latch.await()
        logger.info("가장 오래 걸린 시간: ${TimeUnit.NANOSECONDS.toMillis(maxWorkingTime.get())} ms")

        // then: 1명만 좌석 점유에 성공
        Assertions.assertThat(results.values.count { it }).isEqualTo(1)
        Assertions.assertThat(results.values.count { !it }).isEqualTo(userIds.count() - 1)

        executor.shutdown()
    }
}
