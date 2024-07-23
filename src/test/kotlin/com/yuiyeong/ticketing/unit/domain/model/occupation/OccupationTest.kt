package com.yuiyeong.ticketing.unit.domain.model.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import org.assertj.core.api.Assertions
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

class OccupationTest {
    @Test
    fun `should expire an occupation`() {
        // given
        val userId = 21L
        val now = ZonedDateTime.now().asUtc
        val occupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when
        val result = occupation.expire()

        // then
        Assertions.assertThat(result.status).isEqualTo(OccupationStatus.EXPIRED)
    }

    @Test
    fun `should throw OccupationExpiredException when trying to expire an expired occupation`() {
        // given
        val userId = 112L
        val now = ZonedDateTime.now().asUtc
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
        val now = ZonedDateTime.now().asUtc
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
        val now = ZonedDateTime.now().asUtc
        val occupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when
        val result = occupation.release(now)

        // then
        Assertions.assertThat(result.status).isEqualTo(OccupationStatus.RELEASED)
    }

    @Test
    fun `should throw OccupationExpiredException when trying to release an expired occupation`() {
        // given
        val userId = 1L
        val now = ZonedDateTime.now().asUtc
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
        val now = ZonedDateTime.now().asUtc.minusHours(1)
        val expiredOccupation = createOccupation(userId, now, OccupationStatus.ACTIVE)

        // when & then
        Assertions
            .assertThatThrownBy { expiredOccupation.release(ZonedDateTime.now().asUtc) }
            .isInstanceOf(OccupationAlreadyExpiredException::class.java)
    }

    @Test
    fun `should throw OccupationAlreadyReleaseException when trying to release an released occupation`() {
        // given
        val userId = 1L
        val now = ZonedDateTime.now().asUtc
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
    ): Occupation {
        val allocationStatus =
            when (status) {
                OccupationStatus.ACTIVE -> AllocationStatus.OCCUPIED
                OccupationStatus.RELEASED -> AllocationStatus.RESERVED
                OccupationStatus.EXPIRED -> AllocationStatus.EXPIRED
            }
        val allocations =
            listOf(
                SeatAllocation(
                    id = 0L,
                    userId = userId,
                    seatId = 231L,
                    seatNumber = "23 번",
                    seatPrice = BigDecimal(20000),
                    status = allocationStatus,
                    occupiedAt = createdAt,
                    reservedAt = null,
                    expiredAt = null,
                ),
            )
        return Occupation(
            id = 2L,
            userId = userId,
            concertEventId = 38L,
            allocations = allocations,
            status = status,
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(5),
            expiredAt = null,
        )
    }
}
