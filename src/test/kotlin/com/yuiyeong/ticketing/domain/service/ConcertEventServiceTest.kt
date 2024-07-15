package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class ConcertEventServiceTest {
    @Mock
    private lateinit var concertEventRepository: ConcertEventRepository

    private lateinit var concertEventService: ConcertEventService

    @BeforeEach
    fun beforeEach() {
        concertEventService = ConcertEventService(concertEventRepository)
    }

    @Test
    fun `should throw ConcertEventNotFoundException when trying with unknown concertEventId`() {
        // given
        val unknownId = 12L
        given(concertEventRepository.findOneById(unknownId)).willReturn(null)

        // when & then
        Assertions
            .assertThatThrownBy { concertEventService.getConcertEvent(unknownId) }
            .isInstanceOf(ConcertEventNotFoundException::class.java)

        verify(concertEventRepository).findOneById(unknownId)
    }
}
