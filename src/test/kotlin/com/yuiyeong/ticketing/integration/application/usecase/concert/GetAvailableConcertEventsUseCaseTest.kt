package com.yuiyeong.ticketing.integration.application.usecase.concert

import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableConcertEventsUseCase
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.helper.TestDataFactory
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime
import kotlin.test.Test

@Transactional
@SpringBootTest
class GetAvailableConcertEventsUseCaseTest {
    @Autowired
    private lateinit var getAvailableConcertEventsUseCase: GetAvailableConcertEventsUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var concertEventRepository: ConcertEventRepository

    @Test
    fun `should return available concert events that is within reservation period`() {
        // given
        val now = ZonedDateTime.now().asUtc
        val future = now.plusHours(1)
        val past = now.minusHours(1)
        val concert = concertRepository.save(TestDataFactory.createConcert())
        concertEventRepository.save(TestDataFactory.createConcertEvent(concert, future, future.plusHours(5)))
        val availableEvent = concertEventRepository.save(TestDataFactory.createConcertEvent(concert, past, past.plusHours(5)))

        // when
        val result = getAvailableConcertEventsUseCase.execute(concert.id)

        // then
        Assertions.assertThat(result.count()).isEqualTo(1)
        Assertions.assertThat(result[0].id).isEqualTo(availableEvent.id)
    }

    @Test
    fun `should return empty list when there are no available concerts`() {
        // given
        val concert = concertRepository.save(TestDataFactory.createConcert())

        // when
        val result = getAvailableConcertEventsUseCase.execute(concert.id)

        // then
        Assertions.assertThat(result).isEmpty()
    }
}
