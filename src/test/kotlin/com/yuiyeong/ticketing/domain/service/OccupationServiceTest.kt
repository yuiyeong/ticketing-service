package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.model.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.OccupationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class OccupationServiceTest {
    @Mock
    private lateinit var occupationRepository: OccupationRepository

    private lateinit var occupationService: OccupationService

    @BeforeEach
    fun beforeEach() {
        occupationService = OccupationService(occupationRepository)
    }

    @Test
    fun `should return occupation that has userId and seatIds`() {
        // given
        val userId = 123L
        val seatIds = listOf(82L)
        given(occupationRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Occupation>(0)
            savedOne.copy(id = 1L) // Simulate ID assignment
        }
        // when
        val occupation = occupationService.createOccupation(userId, seatIds)

        // then
        Assertions.assertThat(occupation.userId).isEqualTo(userId)
        Assertions.assertThat(occupation.seatIds).isEqualTo(seatIds)
        Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.ACTIVE)
        Assertions.assertThat(occupation.expiresAt).isAfter(occupation.createdAt)

        verify(occupationRepository).save(
            argThat { it -> it.userId == userId && it.seatIds == seatIds },
        )
    }

    @Test
    fun `should release occupation with userId and seatIds`() {
        // given
        val userId = 12L
        val seatIds = listOf(42L, 45L)
        val occupation = Occupation.create(userId, seatIds)
        given(occupationRepository.findOneByUserIdAndSeatIds(userId, seatIds)).willReturn(occupation)
        given(occupationRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Occupation>(0)
            savedOne.copy(id = 1L) // Simulate ID assignment
        }

        // when
        val releasedOne = occupationService.release(userId, seatIds)

        // then
        Assertions.assertThat(releasedOne.userId).isEqualTo(userId)
        Assertions.assertThat(releasedOne.seatIds).isEqualTo(seatIds)
        Assertions.assertThat(releasedOne.status).isEqualTo(OccupationStatus.RELEASED)

        verify(occupationRepository).findOneByUserIdAndSeatIds(userId, seatIds)
        verify(occupationRepository).save(
            argThat { it -> it.userId == userId && it.seatIds == seatIds },
        )
    }

    @Test
    fun `should throw OccupationNotFoundException when trying to releasing unknown user Id and unknown seat ids`() {
        // given
        val unknownUserId = 12L
        val unknownSeatIds = listOf(2L)

        // when & then
        Assertions
            .assertThatThrownBy { occupationService.release(unknownUserId, unknownSeatIds) }
            .isInstanceOf(OccupationNotFoundException::class.java)

        verify(occupationRepository).findOneByUserIdAndSeatIds(unknownUserId, unknownSeatIds)
    }

    @Test
    fun `should return expired occupations`() {
        // given
        val createdAt = ZonedDateTime.now().minusHours(1)
        val occupation1 = createOccupation(11L, listOf(24L), createdAt = createdAt)
        val occupation2 = createOccupation(12L, listOf(42L, 43L), createdAt = createdAt)
        given(occupationRepository.findAllByExpiresAtBefore(any())).willReturn(listOf(occupation1, occupation2))
        given(occupationRepository.saveAll(any())).willAnswer { invocation ->
            val savedOnes = invocation.getArgument<List<Occupation>>(0)
            println(savedOnes[0].expiredAt)
            savedOnes.mapIndexed { index, occupation -> occupation.copy((index + 1).toLong()) }
        }

        // when
        val expiredOccupations = occupationService.expireOverdueOccupations()

        // then
        Assertions.assertThat(expiredOccupations.count()).isEqualTo(2)
        Assertions.assertThat(expiredOccupations[0].userId).isEqualTo(occupation1.userId)
        Assertions.assertThat(expiredOccupations[0].seatIds).isEqualTo(occupation1.seatIds)
        Assertions.assertThat(expiredOccupations[0].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(expiredOccupations[0].expiredAt).isNotNull()
        Assertions.assertThat(expiredOccupations[1].userId).isEqualTo(occupation2.userId)
        Assertions.assertThat(expiredOccupations[1].seatIds).isEqualTo(occupation2.seatIds)
        Assertions.assertThat(expiredOccupations[1].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(expiredOccupations[1].expiredAt).isNotNull()

        verify(occupationRepository).findAllByExpiresAtBefore(any())
        verify(occupationRepository).saveAll(
            argThat { it ->
                it.size == 2 &&
                    it[0].userId == occupation1.userId &&
                    it[1].userId == occupation2.userId
            },
        )
    }

    private fun createOccupation(
        userId: Long,
        seatIds: List<Long>,
        status: OccupationStatus = OccupationStatus.ACTIVE,
        createdAt: ZonedDateTime = ZonedDateTime.now(),
    ): Occupation =
        Occupation(
            0L,
            userId,
            seatIds,
            status,
            createdAt,
            createdAt.plusMinutes(Occupation.EXPIRATION_MINUTES),
        )
}
