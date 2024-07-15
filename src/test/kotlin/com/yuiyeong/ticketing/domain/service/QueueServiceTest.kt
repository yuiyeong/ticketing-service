package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus
import com.yuiyeong.ticketing.domain.repository.WaitingEntryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {
    @Mock
    private lateinit var entryRepository: WaitingEntryRepository

    private lateinit var queueService: QueueService

    @BeforeEach
    fun beforeEach() {
        queueService = QueueService(entryRepository)
    }

    @Nested
    inner class EnterTest {
        @Test
        fun `should enter a queue as PROCESSING`() {
            // given
            val userId = 321L
            given(entryRepository.findLastWaitingPosition()).willReturn(0)
            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<WaitingEntry>(0)
                savedOne.copy(id = 1L)
            }

            // when
            val entry = queueService.enter(userId)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(WaitingEntryStatus.PROCESSING)
            Assertions.assertThat(entry.position).isEqualTo(0)
            Assertions.assertThat(entry.processingStartedAt).isEqualTo(entry.enteredAt)

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).save(argThat { it -> it.userId == userId })
        }

        @Test
        fun `should enter a queue as WAITING`() {
            // given
            val userId = 32L
            given(entryRepository.findLastWaitingPosition()).willReturn(0)
            given(
                entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING),
            ).willReturn(List(QueueService.MAX_ACTIVE_ENTRIES) { mock() })
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<WaitingEntry>(0)
                savedOne.copy(id = 1L)
            }

            // when
            val entry = queueService.enter(userId)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(WaitingEntryStatus.WAITING)
            Assertions.assertThat(entry.position).isEqualTo(1)
            Assertions.assertThat(entry.enteredAt).isNotNull()
            Assertions.assertThat(entry.processingStartedAt).isNull()

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).save(argThat { it -> it.userId == userId })
        }

        @Test
        fun `should generate unique token for each entry`() {
            // given
            val userId1 = 1L
            val userId2 = 2L

            given(entryRepository.findLastWaitingPosition()).willReturn(0).willReturn(1)
            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedEntry = invocation.getArgument<WaitingEntry>(0)
                savedEntry.copy(id = savedEntry.userId)
            }

            // when
            val entry1 = queueService.enter(userId1)
            val entry2 = queueService.enter(userId2)

            // then
            Assertions.assertThat(entry1.token).isNotEqualTo(entry2.token)
            verify(entryRepository, times(2)).findLastWaitingPosition()
            verify(entryRepository, times(2)).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository, times(2)).save(argThat { it -> it.userId in listOf(userId1, userId2) })
        }

        @Test
        fun `should reenter a queue as WAITING when trying to enter again`() {
            // given
            val userId = 18L
            val lastPosition = 1L
            given(entryRepository.findLastWaitingPosition()).willReturn(lastPosition)
            val mockEntries = List<WaitingEntry>(QueueService.MAX_ACTIVE_ENTRIES) { mock() }
            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(mockEntries)
            val userEntry = createWaitingEntry(userId, 1)
            given(
                entryRepository.findAllByUserIdWithStatus(
                    userId,
                    WaitingEntryStatus.PROCESSING,
                    WaitingEntryStatus.WAITING,
                ),
            ).willReturn(listOf(userEntry))
            given(entryRepository.findOneByToken(userEntry.token)).willReturn(userEntry)
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<WaitingEntry>(0)
                savedOne.copy(id = 1L)
            }

            // when
            val entry = queueService.enter(userId)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(WaitingEntryStatus.WAITING)
            Assertions.assertThat(entry.token).isNotEqualTo(userEntry.token)
            Assertions.assertThat(entry.position).isEqualTo(lastPosition + 1)
            Assertions.assertThat(entry.enteredAt).isNotNull()
            Assertions.assertThat(entry.processingStartedAt).isNull()

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findOneByToken(userEntry.token)
            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).findAllByUserIdWithStatus(
                userId,
                WaitingEntryStatus.PROCESSING,
                WaitingEntryStatus.WAITING,
            )
            verify(entryRepository, times(2)).save(argThat { it -> it.userId == userId })
        }

        @Test
        fun `should reenter a queue as WAITING when trying to enter again even if entry is PROCESSING`() {
            val userId = 18L
            val lastPosition = 4L
            given(entryRepository.findLastWaitingPosition()).willReturn(lastPosition)
            val mockEntries = List<WaitingEntry>(QueueService.MAX_ACTIVE_ENTRIES) { mock() }
            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(mockEntries)
            val userEntry = createWaitingEntry(userId, 0, status = WaitingEntryStatus.PROCESSING)
            given(
                entryRepository.findAllByUserIdWithStatus(
                    userId,
                    WaitingEntryStatus.PROCESSING,
                    WaitingEntryStatus.WAITING,
                ),
            ).willReturn(listOf(userEntry))
            given(entryRepository.findOneByToken(userEntry.token)).willReturn(userEntry)
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<WaitingEntry>(0)
                savedOne.copy(id = 1L)
            }

            // when
            val entry = queueService.enter(userId)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(WaitingEntryStatus.WAITING)
            Assertions.assertThat(entry.token).isNotEqualTo(userEntry.token)
            Assertions.assertThat(entry.position).isEqualTo(lastPosition + 1)
            Assertions.assertThat(entry.enteredAt).isNotNull()
            Assertions.assertThat(entry.processingStartedAt).isNull()

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findOneByToken(userEntry.token)
            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).findAllByUserIdWithStatus(
                userId,
                WaitingEntryStatus.PROCESSING,
                WaitingEntryStatus.WAITING,
            )
            verify(entryRepository, times(2)).save(argThat { it -> it.userId == userId })
        }
    }

    @Nested
    inner class ExitTest {
        @Test
        fun `should exit a queue by changing status to EXITED`() {
            // given
            val userId = 213L
            val userEntry = createWaitingEntry(userId, 1)
            val token = userEntry.token
            given(entryRepository.findOneByToken(token)).willReturn(userEntry)
            given(entryRepository.save(any())).willAnswer { it.getArgument<WaitingEntry>(0) }

            // when
            val exitedOne = queueService.exit(token)

            // then
            Assertions.assertThat(exitedOne.token).isEqualTo(token)
            Assertions.assertThat(exitedOne.status).isEqualTo(WaitingEntryStatus.EXITED)
            Assertions.assertThat(exitedOne.exitedAt).isNotNull()

            verify(entryRepository).findOneByToken(token)
        }

        @Test
        fun `should throw InvalidTokenException when trying to exit with unknown token`() {
            // given
            val unknownToken = "invalid_token"
            given(entryRepository.findOneByToken(unknownToken)).willReturn(null)

            // when & then
            Assertions
                .assertThatThrownBy { queueService.exit(unknownToken) }
                .isInstanceOf(InvalidTokenException::class.java)

            verify(entryRepository).findOneByToken(unknownToken)
        }
    }

    @Nested
    inner class EntryInfoTest {
        @Test
        fun `should return WaitingEntry when valid token is provided`() {
            // given
            val userId = 1L
            val position = 5L
            val status = WaitingEntryStatus.WAITING
            val entry = WaitingEntry.create(userId, position, status)
            val token = entry.token

            given(entryRepository.findOneByToken(token)).willReturn(entry)

            // when
            val enteredOne = queueService.getWaitingEntry(token)

            // then
            Assertions.assertThat(enteredOne.userId).isEqualTo(userId)
            Assertions.assertThat(enteredOne.token).isEqualTo(token)
            Assertions.assertThat(enteredOne.position).isEqualTo(position)
            Assertions.assertThat(enteredOne.status).isEqualTo(status)

            verify(entryRepository).findOneByToken(token)
        }

        @Test
        fun `should throw InvalidTokenException when invalid token is provided`() {
            // given
            val invalidToken = "invalidToken"
            given(entryRepository.findOneByToken(invalidToken)).willReturn(null)

            // when & then
            Assertions
                .assertThatThrownBy { queueService.getWaitingEntry(invalidToken) }
                .isInstanceOf(InvalidTokenException::class.java)

            verify(entryRepository).findOneByToken(invalidToken)
        }

        @Test
        fun `should return correct data for expired entry`() {
            // given
            val userId = 3L
            val position = 0L
            val status = WaitingEntryStatus.EXPIRED
            val entry = WaitingEntry.create(userId, position, status)
            val token = entry.token

            given(entryRepository.findOneByToken(token)).willReturn(entry)

            // when
            val foundOne = queueService.getWaitingEntry(token)

            // then
            Assertions.assertThat(foundOne.userId).isEqualTo(userId)
            Assertions.assertThat(foundOne.token).isEqualTo(token)
            Assertions.assertThat(foundOne.position).isEqualTo(0)
            Assertions.assertThat(foundOne.status).isEqualTo(WaitingEntryStatus.EXPIRED)

            verify(entryRepository).findOneByToken(token)
        }
    }

    @Nested
    inner class ActivationEntriesTest {
        @Test
        fun `should return activated entries when activating waiting entries`() {
            // given
            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(emptyList())

            val entry1 = WaitingEntry.create(11L, 3, WaitingEntryStatus.WAITING)
            val entry2 = WaitingEntry.create(21L, 4, WaitingEntryStatus.WAITING)
            val entry3 = WaitingEntry.create(31L, 5, WaitingEntryStatus.WAITING)

            given(
                entryRepository.findAllByStatusOrderByPosition(
                    WaitingEntryStatus.WAITING,
                    QueueService.MAX_ACTIVE_ENTRIES,
                ),
            ).willReturn(listOf(entry1, entry2, entry3))
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
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

            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).findAllByStatusOrderByPosition(
                WaitingEntryStatus.WAITING,
                QueueService.MAX_ACTIVE_ENTRIES,
            )
            verify(entryRepository).saveAll(any())
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

            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(processingEntryList)
            given(entryRepository.findAllByStatusOrderByPosition(eq(WaitingEntryStatus.WAITING), any())).willReturn(
                waitingEntryList,
            )
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
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

            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository).findAllByStatusOrderByPosition(
                eq(WaitingEntryStatus.WAITING),
                eq(expectedActivatedCount),
            )
            verify(entryRepository).saveAll(any())
        }

        @Test
        fun `should not activate any entries when max processing limit is reached`() {
            // given
            val currentProcessingEntries = 10

            val processingEntryList =
                (1..currentProcessingEntries).map {
                    WaitingEntry.create(it.toLong(), 0, WaitingEntryStatus.PROCESSING)
                }

            given(entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)).willReturn(processingEntryList)

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(activatedEntries).isEmpty()

            verify(entryRepository).findAllByStatus(WaitingEntryStatus.PROCESSING)
            verify(entryRepository, never()).findAllByStatusOrderByPosition(any(), any())
            verify(entryRepository, never()).save(any())
        }
    }

    @Nested
    inner class ExpireOverdueEntries {
        @Test
        fun `should expire overdue entries and return them`() {
            // given
            val overdueEntry1 = createWaitingEntry(1L, 1L, 5L, WaitingEntryStatus.WAITING)
            val overdueEntry2 = createWaitingEntry(2L, 2L, 12L, WaitingEntryStatus.PROCESSING)

            val overdueEntries = listOf(overdueEntry1, overdueEntry2)
            given(
                entryRepository.findAllByExpiresAtBeforeAndStatus(
                    any(),
                    eq(WaitingEntryStatus.PROCESSING),
                    eq(WaitingEntryStatus.WAITING),
                ),
            ).willReturn(overdueEntries)
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
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

            verify(
                entryRepository,
            ).findAllByExpiresAtBeforeAndStatus(
                any(),
                eq(WaitingEntryStatus.PROCESSING),
                eq(WaitingEntryStatus.WAITING),
            )
            verify(entryRepository).saveAll(any())
        }

        @Test
        fun `should return empty list when no overdue entries`() {
            // given
            given(
                entryRepository.findAllByExpiresAtBeforeAndStatus(
                    any(),
                    eq(WaitingEntryStatus.PROCESSING),
                    eq(WaitingEntryStatus.WAITING),
                ),
            ).willReturn(emptyList())

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).isEmpty()

            verify(
                entryRepository,
            ).findAllByExpiresAtBeforeAndStatus(
                any(),
                eq(WaitingEntryStatus.PROCESSING),
                eq(WaitingEntryStatus.WAITING),
            )
            verify(entryRepository, never()).save(any())
        }
    }

    private fun createWaitingEntry(
        userId: Long,
        position: Long,
        id: Long = 53L,
        status: WaitingEntryStatus = WaitingEntryStatus.WAITING,
        createdAt: ZonedDateTime = ZonedDateTime.now(),
    ) = WaitingEntry(
        id = id,
        userId = userId,
        token = WaitingEntry.generateToken(userId, position, createdAt.plusMinutes(WaitingEntry.EXPIRATION_MINUTES)),
        position = position,
        status = status,
        expiresAt = createdAt.plusMinutes(WaitingEntry.EXPIRATION_MINUTES),
        enteredAt = createdAt,
        processingStartedAt = null,
        exitedAt = null,
        expiredAt = null,
    )
}
