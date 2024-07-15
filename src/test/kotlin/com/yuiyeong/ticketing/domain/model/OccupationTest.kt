package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import org.assertj.core.api.Assertions
import java.time.ZonedDateTime
import kotlin.test.Test

class OccupationTest {
    @Test
    fun `should expire an occupation`() {
        // given
        val userId = 21L
        val now = ZonedDateTime.now()
        val occupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when
        occupation.expire()

        // then
        Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.EXPIRED)
    }

    @Test
    fun `should throw OccupationExpiredException when trying to expire an expired occupation`() {
        // given
        val userId = 112L
        val now = ZonedDateTime.now()
        val expiredOccupation = createOccupation(userId, now, OccupationStatus.EXPIRED)

        // when & then
        Assertions
            .assertThatThrownBy { expiredOccupation.expire() }
            .isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyReleaseException when trying to expire an released occupation`() {
        // given
        val userId = 12L
        val now = ZonedDateTime.now()
        val releasedOccupation = createOccupation(userId, now, OccupationStatus.RELEASED)

        // when & then
        Assertions
            .assertThatThrownBy { releasedOccupation.expire() }
            .isInstanceOf(OccupationAlreadyReleaseException::class.java)
    }

    @Test
    fun `should release an occupation`() {
        // given
        val userId = 2L
        val now = ZonedDateTime.now()
        val occupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when
        occupation.release(now)

        // then
        Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.RELEASED)
    }

    @Test
    fun `should throw OccupationExpiredException when trying to release an expired occupation`() {
        // given
        val userId = 1L
        val now = ZonedDateTime.now()
        val expiredOccupation = createOccupation(userId, now, OccupationStatus.EXPIRED)

        // when & then
        Assertions
            .assertThatThrownBy { expiredOccupation.release(now) }
            .isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationExpiredException when trying to release an occupation after expiresAt`() {
        // given
        val userId = 1L
        val now = ZonedDateTime.now().minusHours(1)
        val expiredOccupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when & then
        Assertions
            .assertThatThrownBy { expiredOccupation.release(ZonedDateTime.now()) }
            .isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyReleaseException when trying to release an released occupation`() {
        // given
        val userId = 1L
        val now = ZonedDateTime.now()
        val releasedOccupation = createOccupation(userId, now, OccupationStatus.RELEASED)

        // when & then
        Assertions
            .assertThatThrownBy { releasedOccupation.release(now) }
            .isInstanceOf(OccupationAlreadyReleaseException::class.java)
    }

    private fun createOccupation(
        userId: Long,
        createdAt: ZonedDateTime,
        status: OccupationStatus,
    ): Occupation =
        Occupation(
            2L,
            userId,
            listOf(131L),
            status,
            createdAt,
            createdAt.plusMinutes(Occupation.EXPIRATION_MINUTES),
        )
}
