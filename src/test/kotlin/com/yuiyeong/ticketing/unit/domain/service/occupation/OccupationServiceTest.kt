package com.yuiyeong.ticketing.unit.domain.service.occupation

import com.yuiyeong.ticketing.TestDataFactory.createSeat
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class OccupationServiceTest {
    @Mock
    private lateinit var occupationRepository: OccupationRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    private lateinit var occupationService: OccupationService

    @BeforeEach
    fun beforeEach() {
        occupationService = OccupationService(5, occupationRepository, seatRepository)
    }

    @Test
    fun `should return occupation that has userId and seatIds`() {
        // given
        val userId = 123L
        val seatIds = listOf(82L)
        val seats = listOf(createSeat())
        given(seatRepository.findAllAvailableByIdsWithLock(seatIds)).willReturn(seats)
        given(occupationRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<Occupation>(0)
            savedOne.copy(id = 1L) // Simulate ID assignment
        }
        // when
        val occupation = occupationService.occupy(userId, 4L, seatIds)

        // then
        Assertions.assertThat(occupation.userId).isEqualTo(userId)
        Assertions.assertThat(occupation.status).isEqualTo(OccupationStatus.ACTIVE)
        Assertions.assertThat(occupation.expiresAt).isAfter(occupation.createdAt)

        verify(seatRepository).findAllAvailableByIdsWithLock(seatIds)
        verify(occupationRepository).save(
            argThat { it -> it.userId == userId && it.status == OccupationStatus.ACTIVE },
        )
    }

    @Test
    fun `should release occupation with userId and seatIds`() {
        // given
        val userId = 12L
        val occupationId = 98L
        val seatIds = listOf(42L, 45L)
        val occupation = createOccupation(userId, seatIds, id = occupationId)
        given(occupationRepository.findOneByIdWithLock(occupationId)).willReturn(occupation)
        given(occupationRepository.save(any())).willAnswer { invocation -> invocation.getArgument<Occupation>(0) }

        // when
        val releasedOne = occupationService.release(userId, occupationId)

        // then
        Assertions.assertThat(releasedOne.userId).isEqualTo(userId)
        Assertions.assertThat(releasedOne.allocations.count()).isEqualTo(occupation.allocations.count())
        releasedOne.allocations.forEach {
            Assertions.assertThat(it.status).isEqualTo(AllocationStatus.RESERVED)
        }
        Assertions.assertThat(releasedOne.status).isEqualTo(OccupationStatus.RELEASED)

        verify(occupationRepository).findOneByIdWithLock(occupationId)
        verify(occupationRepository).save(
            argThat { it -> it.id == occupationId && it.userId == userId },
        )
    }

    @Test
    fun `should throw OccupationNotFoundException when trying to releasing unknown user Id and unknown seat ids`() {
        // given
        val unknownUserId = 12L
        val unknownOccupationId = 2L

        // when & then
        Assertions
            .assertThatThrownBy { occupationService.release(unknownUserId, unknownOccupationId) }
            .isInstanceOf(OccupationNotFoundException::class.java)

        verify(occupationRepository).findOneByIdWithLock(unknownOccupationId)
    }

    @Test
    fun `should return expired occupations`() {
        // given
        val createdAt = ZonedDateTime.now().asUtc.minusHours(1)
        val occupation1 = createOccupation(11L, listOf(24L), createdAt = createdAt)
        val occupation2 = createOccupation(12L, listOf(42L, 43L), createdAt = createdAt)
        given(occupationRepository.findAllByExpiresAtBeforeWithLock(any())).willReturn(listOf(occupation1, occupation2))
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
        Assertions.assertThat(expiredOccupations[0].allocations.count()).isEqualTo(occupation1.allocations.count())
        expiredOccupations[0].allocations.forEach {
            Assertions.assertThat(it.status).isEqualTo(AllocationStatus.EXPIRED)
        }
        Assertions.assertThat(expiredOccupations[0].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(expiredOccupations[0].expiredAt).isNotNull()

        Assertions.assertThat(expiredOccupations[1].userId).isEqualTo(occupation2.userId)
        Assertions.assertThat(expiredOccupations[1].allocations.count()).isEqualTo(occupation2.allocations.count())
        expiredOccupations[1].allocations.forEach {
            Assertions.assertThat(it.status).isEqualTo(AllocationStatus.EXPIRED)
        }
        Assertions.assertThat(expiredOccupations[1].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(expiredOccupations[1].expiredAt).isNotNull()

        verify(occupationRepository).findAllByExpiresAtBeforeWithLock(any())
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
        id: Long = 0L,
        status: OccupationStatus = OccupationStatus.ACTIVE,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Occupation {
        val allocationStatus =
            if (status == OccupationStatus.ACTIVE) AllocationStatus.OCCUPIED else AllocationStatus.valueOf(status.name)
        val allocations =
            seatIds.map {
                SeatAllocation(
                    id = it,
                    userId = userId,
                    seatId = it,
                    seatNumber = "$it 번",
                    seatPrice = BigDecimal(20000),
                    status = allocationStatus,
                    occupiedAt = createdAt,
                    expiredAt = null,
                    reservedAt = null,
                )
            }
        return Occupation(
            id = id,
            userId = userId,
            concertEventId = 8L,
            allocations = allocations,
            status = status,
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(5),
            expiredAt = null,
        )
    }
}
