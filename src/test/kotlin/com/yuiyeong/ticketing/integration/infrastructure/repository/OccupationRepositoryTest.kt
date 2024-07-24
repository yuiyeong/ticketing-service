package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class OccupationRepositoryTest {
    @Autowired
    private lateinit var occupationRepository: OccupationRepository

    @Test
    fun `should return occupation that has new id after saving it`() {
        // given
        val occupation = createOccupation(21L, listOf(2L, 1L))

        // when
        val savedOne = occupationRepository.save(occupation)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(occupation.id)
        Assertions.assertThat(savedOne.userId).isEqualTo(occupation.userId)
        Assertions.assertThat(savedOne.allocations.count()).isEqualTo(occupation.allocations.count())
        savedOne.allocations.forEachIndexed { idx, allocation ->
            Assertions.assertThat(allocation.id).isNotEqualTo(occupation.allocations[idx].id)
            Assertions.assertThat(allocation.seatId).isEqualTo(occupation.allocations[idx].seatId)
        }
        Assertions.assertThat(savedOne.status).isEqualTo(occupation.status)
        Assertions.assertThat(savedOne.expiredAt).isEqualTo(occupation.expiredAt)
    }

    @Test
    fun `should return released occupations after releasing and saving it`() {
        // given
        val occupation = createOccupation(78L, listOf(1L, 32L))
        val savedOne = occupationRepository.save(occupation)

        // when
        val updatedOne = occupationRepository.save(savedOne.release(ZonedDateTime.now().asUtc))

        // then
        Assertions.assertThat(updatedOne.id).isEqualTo(savedOne.id)
        Assertions.assertThat(updatedOne.status).isNotEqualTo(occupation.status)
        Assertions.assertThat(updatedOne.status).isEqualTo(OccupationStatus.RELEASED)
    }

    @Test
    fun `should return expired occupations after expiring and saving them`() {
        // given
        val pastCreatedAt = ZonedDateTime.now().asUtc.minusMinutes(10)
        val occupations = mutableListOf<Occupation>()
        occupations.add(
            occupationRepository.save(createOccupation(3L, listOf(11L, 2L), pastCreatedAt)),
        )
        occupations.add(
            occupationRepository.save(createOccupation(2L, listOf(23L, 11L), pastCreatedAt)),
        )

        // when
        val updatedOnes = occupationRepository.saveAll(occupations.map { it.expire() })

        // then
        Assertions.assertThat(updatedOnes.count()).isEqualTo(occupations.count())
        Assertions.assertThat(updatedOnes[0].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(updatedOnes[0].expiredAt).isNotNull()
        Assertions.assertThat(updatedOnes[1].status).isEqualTo(OccupationStatus.EXPIRED)
        Assertions.assertThat(updatedOnes[1].expiredAt).isNotNull()
    }

    @Nested
    inner class QueryTest {
        private val evenUserId = 34L
        private val oddUserId = 211L

        private lateinit var oddUserOccupations: List<Occupation>
        private lateinit var evenUserOccupations: List<Occupation>

        @BeforeEach
        fun beforeEach() {
            // seed data
            val oddUserData = mutableListOf<Occupation>()
            val evenUserData = mutableListOf<Occupation>()
            val pastCreatedAt = ZonedDateTime.now().asUtc.minusHours(1)
            (0..<10).forEach {
                val userId = if (it > 4) oddUserId else evenUserId
                val data = if (it > 4) oddUserData else evenUserData
                if (it % 2 == 0) {
                    val seatIds = listOf((it + 1).toLong(), (it + 1).toLong() * 2)
                    data.add(createOccupation(userId, seatIds))
                } else {
                    val seatIds = listOf((it + 1).toLong())
                    data.add(createOccupation(userId, seatIds, pastCreatedAt.plusMinutes(it.toLong())))
                }
            }
            oddUserOccupations = occupationRepository.saveAll(oddUserData)
            evenUserOccupations = occupationRepository.saveAll(evenUserData)
        }

        @Test
        fun `should return certain Occupation that has userEvenId as userId and seatIds`() {
            // given
            val occupation = evenUserOccupations.last()

            // when
            val evenUserOccupation = occupationRepository.findOneById(occupation.id)

            // then
            Assertions.assertThat(evenUserOccupation!!.id).isEqualTo(occupation.id)
            Assertions.assertThat(evenUserOccupation.userId).isEqualTo(evenUserId)
            Assertions.assertThat(evenUserOccupation.allocations.count()).isEqualTo(occupation.allocations.count())
        }

        @Test
        fun `should return certain Occupation that has userOddId as userId`() {
            // given
            val occupation = oddUserOccupations.last()

            // when
            val evenUserOccupation = occupationRepository.findOneById(occupation.id)

            // then
            Assertions.assertThat(evenUserOccupation!!.id).isEqualTo(occupation.id)
            Assertions.assertThat(evenUserOccupation.userId).isEqualTo(oddUserId)
            Assertions.assertThat(evenUserOccupation.allocations.count()).isEqualTo(occupation.allocations.count())
        }

        @Test
        fun `should return all Occupation that has expires before the moment and not EXPIRED`() {
            // given
            val moment = ZonedDateTime.now().asUtc
            val occupationsToBeExpired =
                oddUserOccupations.filter { it.expiresAt < moment } + evenUserOccupations.filter { it.expiresAt < moment }

            // when
            val occupations = occupationRepository.findAllByStatusAndExpiresAtBeforeWithLock(OccupationStatus.ACTIVE, moment)

            // then
            Assertions.assertThat(occupations.count()).isEqualTo(occupationsToBeExpired.count())
            occupations.forEachIndexed { idx, occupation ->
                val expectedOccupation = occupationsToBeExpired[idx]
                Assertions.assertThat(occupation.id).isEqualTo(expectedOccupation.id)
                Assertions.assertThat(occupation.userId).isEqualTo(expectedOccupation.userId)
                Assertions.assertThat(occupation.status).isEqualTo(expectedOccupation.status)
            }
        }
    }

    private fun createOccupation(
        userId: Long,
        seatIds: List<Long>,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ): Occupation {
        val allocations =
            seatIds.map {
                SeatAllocation(
                    id = 0L,
                    userId = userId,
                    seatId = it,
                    seatNumber = "$it 번",
                    seatPrice = BigDecimal(20000),
                    status = AllocationStatus.OCCUPIED,
                    occupiedAt = createdAt,
                    reservedAt = null,
                    expiredAt = null,
                )
            }
        return Occupation(
            id = 0L,
            userId = userId,
            concertEventId = 33L,
            allocations = allocations,
            status = OccupationStatus.ACTIVE,
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(5),
            expiredAt = null,
        )
    }
}
