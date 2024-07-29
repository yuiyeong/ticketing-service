package com.yuiyeong.ticketing.integration.application.usecase.concert

import com.yuiyeong.ticketing.application.usecase.concert.GetConcertsUseCase
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.helper.TestDataFactory
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class GetConcertsUseCaseTest {
    @Autowired
    private lateinit var getConcertsUseCase: GetConcertsUseCase

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Test
    fun `should return all concerts`() {
        // given
        val concert = concertRepository.save(TestDataFactory.createConcert())

        // when
        val result = getConcertsUseCase.execute()

        // then
        Assertions.assertThat(result.count()).isEqualTo(1)
        Assertions.assertThat(result[0].id).isEqualTo(1)
        Assertions.assertThat(result[0].title).isEqualTo(concert.title)
        Assertions.assertThat(result[0].singer).isEqualTo(concert.singer)
        Assertions.assertThat(result[0].description).isEqualTo(concert.description)
    }

    @Test
    fun `should return empty list when there are no concerts`() {
        // when
        val result = getConcertsUseCase.execute()

        // then
        Assertions.assertThat(result).isEmpty()
    }
}
