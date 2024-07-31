package com.yuiyeong.ticketing.unit.domain.infrastructure.jwt

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.config.property.JwtProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.service.queue.TokenService
import com.yuiyeong.ticketing.infrastructure.jwt.JwtTokenService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.time.ZonedDateTime
import kotlin.test.Test

class JwtTokenServiceTest {
    private lateinit var tokenService: TokenService

    @BeforeEach
    fun beforeEach() {
        tokenService = JwtTokenService(JwtProperties())
    }

    @Test
    fun `should return waitingEntity from encrypted string as token`() {
        // given
        val userId = 123L
        val issuedAt = ZonedDateTime.now().asUtc
        val expiresAt = issuedAt.plusMinutes(10)
        val token = tokenService.generateToken(userId, issuedAt, expiresAt)

        // when
        val extractedUserId = tokenService.validateToken(token)

        // then
        Assertions.assertThat(extractedUserId).isEqualTo(userId)
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

        // when & then
        Assertions
            .assertThatThrownBy { tokenService.validateToken(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }
}
