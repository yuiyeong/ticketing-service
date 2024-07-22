package com.yuiyeong.ticketing.unit.domain.service

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import com.yuiyeong.ticketing.domain.service.queue.QueueService
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
import org.mockito.kotlin.verify
import java.time.ZonedDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {
    @Mock
    private lateinit var entryRepository: QueueEntryRepository

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
            given(entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING)).willReturn(emptyList())
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<QueueEntry>(0)
                savedOne.copy(id = 1L)
            }
            val enteredAt = ZonedDateTime.now().asUtc
            val expiresAt = enteredAt.plusMinutes(EXPIRATION_MINUTES)
            val token = "valid_token"

            // when
            val entry = queueService.enter(userId, token, enteredAt, expiresAt)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(QueueEntryStatus.PROCESSING)
            Assertions.assertThat(entry.position).isEqualTo(0)
            Assertions.assertThat(entry.processingStartedAt).isEqualTo(entry.enteredAt)

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findAllByStatus(QueueEntryStatus.PROCESSING)
            verify(entryRepository).save(argThat { it -> it.userId == userId })
        }

        @Test
        fun `should enter a queue as WAITING`() {
            // given
            val userId = 32L
            given(entryRepository.findLastWaitingPosition()).willReturn(0)
            given(
                entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING),
            ).willReturn(List(QueueService.MAX_ACTIVE_ENTRIES) { mock() })
            given(entryRepository.save(any())).willAnswer { invocation ->
                val savedOne = invocation.getArgument<QueueEntry>(0)
                savedOne.copy(id = 1L)
            }
            val enteredAt = ZonedDateTime.now().asUtc
            val expiresAt = enteredAt.plusMinutes(EXPIRATION_MINUTES)
            val token = "valid_token"

            // when
            val entry = queueService.enter(userId, token, enteredAt, expiresAt)

            // then
            Assertions.assertThat(entry.userId).isEqualTo(userId)
            Assertions.assertThat(entry.status).isEqualTo(QueueEntryStatus.WAITING)
            Assertions.assertThat(entry.position).isEqualTo(1)
            Assertions.assertThat(entry.enteredAt).isNotNull()
            Assertions.assertThat(entry.processingStartedAt).isNull()

            verify(entryRepository).findLastWaitingPosition()
            verify(entryRepository).findAllByStatus(QueueEntryStatus.PROCESSING)
            verify(entryRepository).save(argThat { it -> it.userId == userId })
        }
    }

    @Nested
    inner class ExitTest {
        @Test
        fun `should exit a queue by changing status to EXITED`() {
            // given
            val userId = 213L
            val userEntry = createQueueEntry(userId, 1)
            given(entryRepository.findOneByIdWithLock(userEntry.id)).willReturn(userEntry)
            given(entryRepository.save(any())).willAnswer { it.getArgument<QueueEntry>(0) }

            // when
            val exitedOne = queueService.exit(userEntry.id)

            // then
            Assertions.assertThat(exitedOne.token).isEqualTo(userEntry.token)
            Assertions.assertThat(exitedOne.status).isEqualTo(QueueEntryStatus.EXITED)
            Assertions.assertThat(exitedOne.exitedAt).isNotNull()

            verify(entryRepository).findOneByIdWithLock(userEntry.id)
        }

        @Test
        fun `should throw InvalidTokenException when trying to exit with unknown token`() {
            // given
            val unknownEntryId = 5234L
            given(entryRepository.findOneByIdWithLock(unknownEntryId)).willReturn(null)

            // when & then
            Assertions
                .assertThatThrownBy { queueService.exit(unknownEntryId) }
                .isInstanceOf(InvalidTokenException::class.java)

            verify(entryRepository).findOneByIdWithLock(unknownEntryId)
        }
    }

    @Nested
    inner class EntryInfoTest {
        @Test
        fun `should return queueEntry when valid token is provided`() {
            // given
            val userId = 1L
            val position = 5L
            val status = QueueEntryStatus.WAITING
            val entry = createQueueEntry(userId, position, status = status)
            val token = entry.token

            given(entryRepository.findOneByToken(token)).willReturn(entry)

            // when
            val enteredOne = queueService.getEntry(token)

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
                .assertThatThrownBy { queueService.getEntry(invalidToken) }
                .isInstanceOf(InvalidTokenException::class.java)

            verify(entryRepository).findOneByToken(invalidToken)
        }

        @Test
        fun `should return correct data for expired entry`() {
            // given
            val userId = 3L
            val position = 0L
            val status = QueueEntryStatus.EXPIRED
            val entry = createQueueEntry(userId, position, status = status)
            val token = entry.token

            given(entryRepository.findOneByToken(token)).willReturn(entry)

            // when
            val foundOne = queueService.getEntry(token)

            // then
            Assertions.assertThat(foundOne.userId).isEqualTo(userId)
            Assertions.assertThat(foundOne.token).isEqualTo(token)
            Assertions.assertThat(foundOne.position).isEqualTo(0)
            Assertions.assertThat(foundOne.status).isEqualTo(QueueEntryStatus.EXPIRED)

            verify(entryRepository).findOneByToken(token)
        }
    }

    @Nested
    inner class ActivationEntriesTest {
        @Test
        fun `should return activated entries when activating waiting entries`() {
            // given
            given(entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING)).willReturn(emptyList())

            val entry1 = createQueueEntry(11L, 3, status = QueueEntryStatus.WAITING)
            val entry2 = createQueueEntry(21L, 4, status = QueueEntryStatus.WAITING)
            val entry3 = createQueueEntry(31L, 5, status = QueueEntryStatus.WAITING)

            given(
                entryRepository.findAllByStatusOrderByPositionWithLock(
                    QueueEntryStatus.WAITING,
                    QueueService.MAX_ACTIVE_ENTRIES,
                ),
            ).willReturn(listOf(entry1, entry2, entry3))
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<QueueEntry>>(0)
                savedEntries.mapIndexed { index, queueEntry -> queueEntry.copy(id = (2L + index)) }
            }

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(activatedEntries.size).isEqualTo(3)
            Assertions.assertThat(activatedEntries[0].token).isEqualTo(entry1.token)
            Assertions.assertThat(activatedEntries[1].token).isEqualTo(entry2.token)
            Assertions.assertThat(activatedEntries[2].token).isEqualTo(entry3.token)

            activatedEntries.forEach {
                Assertions.assertThat(it.status).isEqualTo(QueueEntryStatus.PROCESSING)
            }

            verify(entryRepository).findAllByStatus(QueueEntryStatus.PROCESSING)
            verify(entryRepository).findAllByStatusOrderByPositionWithLock(
                QueueEntryStatus.WAITING,
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
                    createQueueEntry(it.toLong(), 0, status = QueueEntryStatus.PROCESSING)
                }

            val queueEntryList =
                (currentProcessingEntries + 1..waitingEntries + currentProcessingEntries).map {
                    createQueueEntry(
                        it.toLong(),
                        (it - currentProcessingEntries).toLong(),
                        status = QueueEntryStatus.WAITING,
                    )
                }

            given(entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING)).willReturn(processingEntryList)
            given(entryRepository.findAllByStatusOrderByPositionWithLock(eq(QueueEntryStatus.WAITING), any())).willReturn(
                queueEntryList,
            )
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<QueueEntry>>(0)
                savedEntries.mapIndexed { index, queueEntry -> queueEntry.copy(id = (2L + index)) }
            }

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            val expectedActivatedCount = QueueService.MAX_ACTIVE_ENTRIES - currentProcessingEntries
            Assertions.assertThat(activatedEntries).hasSize(expectedActivatedCount)

            activatedEntries.forEachIndexed { index, entry ->
                Assertions.assertThat(entry.status).isEqualTo(QueueEntryStatus.PROCESSING)
                Assertions.assertThat(entry.userId).isEqualTo((index + currentProcessingEntries + 1).toLong())
            }

            verify(entryRepository).findAllByStatus(QueueEntryStatus.PROCESSING)
            verify(entryRepository).findAllByStatusOrderByPositionWithLock(
                eq(QueueEntryStatus.WAITING),
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
                    createQueueEntry(it.toLong(), 0, status = QueueEntryStatus.PROCESSING)
                }

            given(entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING)).willReturn(processingEntryList)

            // when
            val activatedEntries = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(activatedEntries).isEmpty()

            verify(entryRepository).findAllByStatus(QueueEntryStatus.PROCESSING)
            verify(entryRepository, never()).findAllByStatusOrderByPositionWithLock(any(), any())
            verify(entryRepository, never()).save(any())
        }
    }

    @Nested
    inner class ExpireOverdueEntries {
        @Test
        fun `should expire overdue entries and return them`() {
            // given
            val overdueEntry1 = createQueueEntry(1L, 1L, 5L, QueueEntryStatus.WAITING)
            val overdueEntry2 = createQueueEntry(2L, 2L, 12L, QueueEntryStatus.PROCESSING)

            val overdueEntries = listOf(overdueEntry1, overdueEntry2)
            given(
                entryRepository.findAllByExpiresAtBeforeAndStatusWithLock(
                    any(),
                    eq(QueueEntryStatus.PROCESSING),
                    eq(QueueEntryStatus.WAITING),
                ),
            ).willReturn(overdueEntries)
            given(entryRepository.saveAll(any())).willAnswer { invocation ->
                val savedEntries = invocation.getArgument<List<QueueEntry>>(0)
                savedEntries.mapIndexed { index, queueEntry -> queueEntry.copy(id = (2L + index)) }
            }

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result[0].userId).isEqualTo(1L)
            Assertions.assertThat(result[1].userId).isEqualTo(2L)
            Assertions.assertThat(result[0].status).isEqualTo(QueueEntryStatus.EXPIRED)
            Assertions.assertThat(result[1].status).isEqualTo(QueueEntryStatus.EXPIRED)
            Assertions.assertThat(result[0].position).isEqualTo(0)
            Assertions.assertThat(result[1].position).isEqualTo(0)

            verify(
                entryRepository,
            ).findAllByExpiresAtBeforeAndStatusWithLock(
                any(),
                eq(QueueEntryStatus.PROCESSING),
                eq(QueueEntryStatus.WAITING),
            )
            verify(entryRepository).saveAll(any())
        }

        @Test
        fun `should return empty list when no overdue entries`() {
            // given
            given(
                entryRepository.findAllByExpiresAtBeforeAndStatusWithLock(
                    any(),
                    eq(QueueEntryStatus.PROCESSING),
                    eq(QueueEntryStatus.WAITING),
                ),
            ).willReturn(emptyList())

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).isEmpty()

            verify(
                entryRepository,
            ).findAllByExpiresAtBeforeAndStatusWithLock(
                any(),
                eq(QueueEntryStatus.PROCESSING),
                eq(QueueEntryStatus.WAITING),
            )
            verify(entryRepository, never()).save(any())
        }
    }

    private fun createQueueEntry(
        userId: Long,
        position: Long,
        id: Long = 53L,
        status: QueueEntryStatus = QueueEntryStatus.WAITING,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ) = QueueEntry(
        id = id,
        userId = userId,
        token = "valid_token_${id}_${userId}_${position}_${createdAt.toEpochSecond()}",
        position = position,
        status = status,
        expiresAt = createdAt.plusMinutes(EXPIRATION_MINUTES),
        enteredAt = createdAt,
        processingStartedAt = null,
        exitedAt = null,
        expiredAt = null,
    )

    companion object {
        const val EXPIRATION_MINUTES = 60L // 60분 뒤 만료
    }
}
