package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntry.Companion.generateToken
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus
import com.yuiyeong.ticketing.domain.repository.WaitingEntryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {
    @Mock
    private lateinit var repository: WaitingEntryRepository

    private lateinit var queueService: QueueService

    @BeforeEach
    fun setup() {
        queueService = QueueService(repository)
    }

    @Nested
    inner class EnteringQueue {
        @Test
        fun `should return processing WaitingEntry when entering the queue at first`() {
            // given
            val userId = 1L
            val expectedPosition = 0L
            val expectedStatus = WaitingEntryStatus.PROCESSING

            given(repository.findLastWaitingPosition()).willReturn(0)
            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())
            given(repository.save(any())).willAnswer { invocation ->
                val savedEntry = invocation.getArgument<WaitingEntry>(0)
                savedEntry.copy(id = 1L) // Simulate ID assignment
            }

            // when
            val enteredOne = queueService.enter(userId)

            // then
            Assertions.assertThat(enteredOne.userId).isEqualTo(userId)
            Assertions.assertThat(enteredOne.position).isEqualTo(expectedPosition)
            Assertions.assertThat(enteredOne.status).isEqualTo(expectedStatus)

            verify(repository).save(
                argThat { entry ->
                    entry.userId == userId &&
                        entry.position == expectedPosition &&
                        entry.status == expectedStatus &&
                        entry.expiresAt.isAfter(ZonedDateTime.now())
                },
            )
        }

        @Test
        fun `should return waiting WaitingEntry when max active entries reached`() {
            // given
            val userId = 2L
            val expectedPosition = 11L
            val expectedStatus = WaitingEntryStatus.WAITING

            given(repository.findLastWaitingPosition()).willReturn(10)
            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING))
                .willReturn(List(10) { mock() })
            given(repository.save(any())).willAnswer { invocation ->
                val savedEntry = invocation.getArgument<WaitingEntry>(0)
                savedEntry.copy(id = 2L)
            }

            // when
            val enteredOne = queueService.enter(userId)

            // then
            Assertions.assertThat(enteredOne.userId).isEqualTo(userId)
            Assertions.assertThat(enteredOne.position).isEqualTo(expectedPosition)
            Assertions.assertThat(enteredOne.status).isEqualTo(expectedStatus)

            verify(repository).save(
                argThat { entry ->
                    entry.userId == userId &&
                        entry.position == expectedPosition &&
                        entry.status == expectedStatus &&
                        entry.expiresAt.isAfter(ZonedDateTime.now())
                },
            )
        }

        @Test
        fun `should generate unique token for each entry`() {
            // given
            val userId1 = 1L
            val userId2 = 2L

            given(repository.findLastWaitingPosition()).willReturn(0).willReturn(1)
            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())
            given(repository.save(any())).willAnswer { invocation ->
                val savedEntry = invocation.getArgument<WaitingEntry>(0)
                savedEntry.copy(id = savedEntry.userId)
            }

            // when
            val entry1 = queueService.enter(userId1)
            val entry2 = queueService.enter(userId2)

            // then
            Assertions.assertThat(entry1.token).isNotEqualTo(entry2.token)
        }

        @Test
        fun `should reissue token for user already in queue`() {
            // given
            val userId = 1L
            val existingEntry = WaitingEntry.create(userId, 5, WaitingEntryStatus.WAITING)
            val newPosition = 11L
            val newEstimatedWaitingTime = newPosition * WaitingEntry.WORKING_MINUTES
            val newStatus = WaitingEntryStatus.WAITING

            given(
                repository.findAllByUserIdWithStatus(
                    userId,
                    WaitingEntryStatus.PROCESSING,
                    WaitingEntryStatus.WAITING,
                ),
            ).willReturn(listOf(existingEntry))
            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING))
                .willReturn(List(10) { mock() })
            given(repository.findOneByToken(existingEntry.token)).willReturn(existingEntry)

            given(repository.findLastWaitingPosition()).willReturn(10)
            given(repository.save(any())).willAnswer { invocation ->
                val savedEntry = invocation.getArgument<WaitingEntry>(0)
                savedEntry.copy(id = 2L)
            }

            // when
            val reenteredOne = queueService.enter(userId)

            // then
            Assertions.assertThat(reenteredOne.userId).isEqualTo(userId)
            Assertions.assertThat(reenteredOne.status).isEqualTo(newStatus)
            Assertions.assertThat(reenteredOne.position).isEqualTo(newPosition)
            Assertions.assertThat(reenteredOne.token).isNotEqualTo(existingEntry.token)

            verify(repository).save(
                argThat { entry ->
                    entry.userId == userId &&
                        entry.position == newPosition &&
                        entry.status == newStatus &&
                        entry.expiresAt.isAfter(ZonedDateTime.now())
                },
            )
        }
    }

    @Nested
    inner class EntryInfo {
        @Test
        fun `should return WaitingEntry when valid token is provided`() {
            // given
            val userId = 1L
            val position = 5L
            val status = WaitingEntryStatus.WAITING
            val entry = WaitingEntry.create(userId, position, status)
            val token = entry.token

            given(repository.findOneByToken(token)).willReturn(entry)

            // when
            val enteredOne = queueService.getEntryInfo(token)

            // then
            Assertions.assertThat(enteredOne.userId).isEqualTo(userId)
            Assertions.assertThat(enteredOne.token).isEqualTo(token)
            Assertions.assertThat(enteredOne.position).isEqualTo(position)
            Assertions.assertThat(enteredOne.status).isEqualTo(status)

            verify(repository).findOneByToken(token)
        }

        @Test
        fun `should throw InvalidTokenException when invalid token is provided`() {
            // given
            val invalidToken = "invalidToken"
            given(repository.findOneByToken(invalidToken)).willReturn(null)

            // when & then
            Assertions
                .assertThatThrownBy { queueService.getEntryInfo(invalidToken) }
                .isInstanceOf(InvalidTokenException::class.java)

            verify(repository).findOneByToken(invalidToken)
        }

        @Test
        fun `should return correct data for expired entry`() {
            // given
            val userId = 3L
            val position = 0L
            val status = WaitingEntryStatus.EXPIRED
            val entry = WaitingEntry.create(userId, position, status)
            val token = entry.token

            given(repository.findOneByToken(token)).willReturn(entry)

            // when
            val foundOne = queueService.getEntryInfo(token)

            // then
            Assertions.assertThat(foundOne.userId).isEqualTo(userId)
            Assertions.assertThat(foundOne.token).isEqualTo(token)
            Assertions.assertThat(foundOne.status).isEqualTo(WaitingEntryStatus.EXPIRED)

            verify(repository).findOneByToken(token)
        }
    }

    @Nested
    inner class ActivationEntries {
        @Test
        fun `should return activated entries when activating waiting entries`() {
            // given
            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())

            val entry1 = WaitingEntry.create(11L, 3, WaitingEntryStatus.WAITING)
            val entry2 = WaitingEntry.create(21L, 4, WaitingEntryStatus.WAITING)
            val entry3 = WaitingEntry.create(31L, 5, WaitingEntryStatus.WAITING)

            given(
                repository.findAllByStatusOrderByPosition(WaitingEntryStatus.WAITING, QueueService.MAX_ACTIVE_ENTRIES),
            ).willReturn(listOf(entry1, entry2, entry3))
            given(repository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<WaitingEntry>>(0)
                savedEntries.mapIndexed { index, waitingEntry -> waitingEntry.copy(id = (2L + index)) }
            }

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(activatedEntries.size).isEqualTo(3)
            Assertions.assertThat(activatedEntries[0].token).isEqualTo(entry1.token)
            Assertions.assertThat(activatedEntries[1].token).isEqualTo(entry2.token)
            Assertions.assertThat(activatedEntries[2].token).isEqualTo(entry3.token)

            activatedEntries.forEach {
                Assertions.assertThat(it.status).isEqualTo(WaitingEntryStatus.PROCESSING)
            }

            verify(repository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(repository).findAllByStatusOrderByPosition(
                WaitingEntryStatus.WAITING,
                QueueService.MAX_ACTIVE_ENTRIES,
            )
            verify(repository).saveAll(any())
        }

        @Test
        fun `should activate correct number of entries when some are already processing`() {
            // given
            val currentProcessingEntries = 3
            val waitingEntries = 7

            val processingEntryList =
                (1..currentProcessingEntries).map {
                    WaitingEntry.create(it.toLong(), 0, WaitingEntryStatus.PROCESSING)
                }

            val waitingEntryList =
                (currentProcessingEntries + 1..waitingEntries + currentProcessingEntries).map {
                    WaitingEntry.create(
                        it.toLong(),
                        (it - currentProcessingEntries).toLong(),
                        WaitingEntryStatus.WAITING,
                    )
                }

            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(processingEntryList)
            given(repository.findAllByStatusOrderByPosition(eq(WaitingEntryStatus.WAITING), any())).willReturn(
                waitingEntryList,
            )
            given(repository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<WaitingEntry>>(0)
                savedEntries.mapIndexed { index, waitingEntry -> waitingEntry.copy(id = (2L + index)) }
            }

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            val expectedActivatedCount = QueueService.MAX_ACTIVE_ENTRIES - currentProcessingEntries
            Assertions.assertThat(activatedEntries).hasSize(expectedActivatedCount)

            activatedEntries.forEachIndexed { index, entry ->
                Assertions.assertThat(entry.status).isEqualTo(WaitingEntryStatus.PROCESSING)
                Assertions.assertThat(entry.userId).isEqualTo((index + currentProcessingEntries + 1).toLong())
            }

            verify(repository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(repository).findAllByStatusOrderByPosition(
                eq(WaitingEntryStatus.WAITING),
                eq(expectedActivatedCount),
            )
            verify(repository).saveAll(any())
        }

        @Test
        fun `should not activate any entries when max processing limit is reached`() {
            // given
            val currentProcessingEntries = 10

            val processingEntryList =
                (1..currentProcessingEntries).map {
                    WaitingEntry.create(it.toLong(), 0, WaitingEntryStatus.PROCESSING)
                }

            given(repository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(processingEntryList)

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(activatedEntries).isEmpty()

            verify(repository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(repository, never()).findAllByStatusOrderByPosition(any(), any())
            verify(repository, never()).save(any())
        }
    }

    @Nested
    inner class ExpireOverdueEntries {
        private fun createOverdueEntry(
            userId: Long,
            position: Long,
            status: WaitingEntryStatus,
            minusMinutes: Long,
        ): WaitingEntry {
            val now = ZonedDateTime.now().minusHours(1)
            val expiresAt = now.minusMinutes(minusMinutes)
            val enteredAt = if (status == WaitingEntryStatus.PROCESSING) now else null
            return WaitingEntry(
                id = 0L,
                userId = userId,
                token = generateToken(userId, position, expiresAt),
                position = position,
                status = status,
                expiresAt = expiresAt,
                enteredAt = now,
                processingStartedAt = enteredAt,
                exitedAt = null,
            )
        }

        @Test
        fun `should expire overdue entries and return them`() {
            // given
            val overdueEntry1 = createOverdueEntry(1L, 1, WaitingEntryStatus.WAITING, 5)
            val overdueEntry2 = createOverdueEntry(2L, 2, WaitingEntryStatus.PROCESSING, 10)

            val overdueEntries = listOf(overdueEntry1, overdueEntry2)

            given(
                repository.findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING),
            ).willReturn(overdueEntries)
            given(repository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<WaitingEntry>>(0)
                savedEntries.mapIndexed { index, waitingEntry -> waitingEntry.copy(id = (2L + index)) }
            }

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result[0].userId).isEqualTo(1L)
            Assertions.assertThat(result[1].userId).isEqualTo(2L)
            Assertions.assertThat(result[0].status).isEqualTo(WaitingEntryStatus.EXPIRED)
            Assertions.assertThat(result[1].status).isEqualTo(WaitingEntryStatus.EXPIRED)
            Assertions.assertThat(result[0].position).isEqualTo(0)
            Assertions.assertThat(result[1].position).isEqualTo(0)

            verify(repository).findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            verify(repository).saveAll(any())
        }

        @Test
        fun `should return empty list when no overdue entries`() {
            // given
            given(
                repository.findOverdueEntriesByStatus(
                    WaitingEntryStatus.PROCESSING,
                    WaitingEntryStatus.WAITING,
                ),
            ).willReturn(emptyList())

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).isEmpty()

            verify(repository).findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            verify(repository, never()).save(any())
        }

        @Test
        fun `should handle exception when trying to expire invalid entry`() {
            // given
            val invalidEntry = createOverdueEntry(1L, 1, WaitingEntryStatus.EXPIRED, 5)

            given(
                repository.findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING),
            ).willReturn(listOf(invalidEntry))

            // when & then
            Assertions
                .assertThatThrownBy { queueService.expireOverdueEntries() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("작업 중이거나 대기 중일 때만 만료할 수 있습니다.")

            verify(repository).findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            verify(repository, never()).save(any())
        }

        @Test
        fun `should not expire entries that are not yet overdue`() {
            // given
            val notOverdueEntry = WaitingEntry.create(1L, 1, WaitingEntryStatus.WAITING)

            given(
                repository.findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING),
            ).willReturn(listOf(notOverdueEntry))

            // when & then
            Assertions
                .assertThatThrownBy { queueService.expireOverdueEntries() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("현재 만료 일시가 지나지 않았습니다.")

            verify(repository).findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            verify(repository, never()).save(any())
        }
    }
}
