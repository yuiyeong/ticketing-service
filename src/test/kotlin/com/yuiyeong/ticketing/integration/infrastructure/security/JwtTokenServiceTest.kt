package com.yuiyeong.ticketing.integration.infrastructure.security

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.QueueEntry
import com.yuiyeong.ticketing.domain.model.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.QueueEntryRepository
import com.yuiyeong.ticketing.domain.service.TokenService
import org.assertj.core.api.Assertions
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
class JwtTokenServiceTest {
    @MockBean
    private lateinit var queueEntryRepository: QueueEntryRepository

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `should return waitingEntity from encrypted string as token`() {
        // given
        val userId = 123L
        val issuedAt = ZonedDateTime.now().asUtc
        val expiresAt = issuedAt.plusMinutes(10)
        val token = tokenService.generateToken(userId, issuedAt, expiresAt)
        val queueEntry =
            QueueEntry(
                id = 213L,
                userId = userId,
                token = token,
                position = 22L,
                status = QueueEntryStatus.WAITING,
                enteredAt = issuedAt,
                expiresAt = expiresAt,
                processingStartedAt = null,
                exitedAt = null,
                expiredAt = null,
            )
        given(queueEntryRepository.findOneByToken(token)).willReturn(queueEntry)

        // when
        val waitingEntity = tokenService.validateToken(token)

        // then
        Assertions.assertThat(waitingEntity.userId).isEqualTo(userId)

        verify(queueEntryRepository).findOneByToken(token)
    }

    @Test
    fun `should throw InvalidTokenException when trying to validateToken with unknown token`() {
        // given
        val unknownToken = "hello_test_token"

        // when & then
        Assertions
            .assertThatThrownBy { tokenService.validateToken(unknownToken) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when trying to validateToken with expired token`() {
        // given
        val userId = 13L
        val issuedAt = ZonedDateTime.now().asUtc.minusDays(1)
        val expiresAt = issuedAt.plusMinutes(10)
        val token = tokenService.generateToken(userId, issuedAt, expiresAt)
        val queueEntry =
            QueueEntry(
                id = 213L,
                userId = userId,
                token = token,
                position = 22L,
                status = QueueEntryStatus.WAITING,
                enteredAt = issuedAt,
                expiresAt = expiresAt,
                processingStartedAt = null,
                exitedAt = null,
                expiredAt = null,
            )
        given(queueEntryRepository.findOneByToken(token)).willReturn(queueEntry)

        // when & then
        Assertions
            .assertThatThrownBy { tokenService.validateToken(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }
}
