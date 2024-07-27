package com.yuiyeong.ticketing.integration.domain.service.queue

import com.yuiyeong.ticketing.TestDataFactory.createQueueEntry
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@Transactional
@Execution(ExecutionMode.CONCURRENT)
class QueueServiceTest {
    @Autowired
    private lateinit var queueService: QueueService

    @Autowired
    private lateinit var queueEntryRepository: QueueEntryRepository

    @Nested
    inner class GetFirstWaitingPositionTest {
        @Test
        fun `should return 0 when queue is empty`() {
            // when
            val result = queueService.getFirstWaitingPosition()

            // then
            Assertions.assertThat(result).isEqualTo(0)
        }

        @Test
        fun `should return correct first waiting position`() {
            // given
            val entries =
                listOf(
                    createQueueEntry(position = 1, status = QueueEntryStatus.WAITING),
                    createQueueEntry(position = 2, status = QueueEntryStatus.WAITING),
                    createQueueEntry(position = 3, status = QueueEntryStatus.WAITING),
                )
            queueEntryRepository.saveAll(entries)

            // when
            val result = queueService.getFirstWaitingPosition()

            // then
            Assertions.assertThat(result).isEqualTo(1)
        }
    }

    @Nested
    inner class EnterTest {
        @Test
        fun `should create new entry with WAITING status when max active entries reached`() {
            // given
            val userId = 1L
            val token = "test-token"
            val enteredAt = ZonedDateTime.now().asUtc
            val expiresAt = enteredAt.plusMinutes(30)

            // Create max active entries
            repeat(QueueService.MAX_ACTIVE_ENTRIES) {
                queueEntryRepository.save(createQueueEntry(status = QueueEntryStatus.PROCESSING))
            }

            // when
            val result = queueService.enter(userId, token, enteredAt, expiresAt)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.token).isEqualTo(token)
            Assertions.assertThat(result.status).isEqualTo(QueueEntryStatus.WAITING)
            Assertions.assertThat(result.position).isEqualTo(1)
        }

        @Test
        fun `should create new entry with PROCESSING status when active entries not full`() {
            // given
            val userId = 1L
            val token = "test-token"
            val enteredAt = ZonedDateTime.now().asUtc
            val expiresAt = enteredAt.plusMinutes(30)

            // when
            val result = queueService.enter(userId, token, enteredAt, expiresAt)

            // then
            Assertions.assertThat(result.userId).isEqualTo(userId)
            Assertions.assertThat(result.token).isEqualTo(token)
            Assertions.assertThat(result.status).isEqualTo(QueueEntryStatus.PROCESSING)
            Assertions.assertThat(result.position).isEqualTo(0)
        }
    }

    @Nested
    inner class ExitTest {
        @Test
        fun `should exit entry successfully`() {
            // given
            val entry = queueEntryRepository.save(createQueueEntry(status = QueueEntryStatus.PROCESSING))

            // when
            val result = queueService.exit(entry.id)

            // then
            Assertions.assertThat(result.status).isEqualTo(QueueEntryStatus.EXITED)
            Assertions.assertThat(result.exitedAt).isNotNull()
        }

        @Test
        fun `should throw exception when entry not found`() {
            // given
            val nonExistentId = 999L

            // when & then
            Assertions
                .assertThatThrownBy {
                    queueService.exit(nonExistentId)
                }.isInstanceOf(InvalidTokenException::class.java)
        }
    }

    @Nested
    inner class GetEntryTest {
        @Test
        fun `should return entry when token is valid`() {
            // given
            val token = "valid-token"
            val entry = queueEntryRepository.save(createQueueEntry(token = token))

            // when
            val result = queueService.getEntry(token)

            // then
            Assertions.assertThat(result.id).isEqualTo(entry.id)
        }

        @Test
        fun `should throw exception when token is null`() {
            // when & then
            Assertions
                .assertThatThrownBy {
                    queueService.getEntry(null)
                }.isInstanceOf(InvalidTokenException::class.java)
        }

        @Test
        fun `should throw exception when token is invalid`() {
            // given
            val invalidToken = "invalid-token"

            // when & then
            Assertions
                .assertThatThrownBy {
                    queueService.getEntry(invalidToken)
                }.isInstanceOf(InvalidTokenException::class.java)
        }
    }

    @Nested
    inner class ActivateWaitingEntriesTest {
        @Test
        fun `should activate waiting entries when there are less than max active entries`() {
            // given
            val waitingEntries =
                listOf(
                    createQueueEntry(status = QueueEntryStatus.WAITING),
                    createQueueEntry(status = QueueEntryStatus.WAITING),
                )
            queueEntryRepository.saveAll(waitingEntries)

            // when
            val result = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result).allMatch { it.status == QueueEntryStatus.PROCESSING }
        }

        @Test
        fun `should not activate any entries when max active entries reached`() {
            // given
            repeat(QueueService.MAX_ACTIVE_ENTRIES) {
                queueEntryRepository.save(createQueueEntry(status = QueueEntryStatus.PROCESSING))
            }
            queueEntryRepository.save(createQueueEntry(status = QueueEntryStatus.WAITING))

            // when
            val result = queueService.activateWaitingEntries()

            // then
            Assertions.assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class ExpireOverdueEntriesTest {
        @Test
        fun `should expire overdue entries`() {
            // given
            val overdueEntries =
                listOf(
                    createQueueEntry(status = QueueEntryStatus.WAITING, expiresAt = ZonedDateTime.now().asUtc.minusMinutes(1)),
                    createQueueEntry(status = QueueEntryStatus.PROCESSING, expiresAt = ZonedDateTime.now().asUtc.minusMinutes(1)),
                )
            queueEntryRepository.saveAll(overdueEntries)

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).hasSize(2)
            Assertions.assertThat(result).allMatch { it.status == QueueEntryStatus.EXPIRED }
        }

        @Test
        fun `should not expire non-overdue entries`() {
            // given
            val nonOverdueEntries =
                listOf(
                    createQueueEntry(status = QueueEntryStatus.WAITING, expiresAt = ZonedDateTime.now().asUtc.plusMinutes(1)),
                    createQueueEntry(status = QueueEntryStatus.PROCESSING, expiresAt = ZonedDateTime.now().asUtc.plusMinutes(1)),
                )
            queueEntryRepository.saveAll(nonOverdueEntries)

            // when
            val result = queueService.expireOverdueEntries()

            // then
            Assertions.assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class DequeueExistingEntriesTest {
        @Test
        fun `should dequeue existing entries for user`() {
            // given
            val userId = 1L
            val entries =
                listOf(
                    createQueueEntry(userId = userId, status = QueueEntryStatus.WAITING),
                    createQueueEntry(userId = userId, status = QueueEntryStatus.PROCESSING),
                )
            queueEntryRepository.saveAll(entries)

            // when
            queueService.dequeueExistingEntries(userId)

            // then
            val remainingEntries =
                queueEntryRepository.findAllByUserIdWithStatus(
                    userId,
                    QueueEntryStatus.WAITING,
                    QueueEntryStatus.PROCESSING,
                )
            Assertions.assertThat(remainingEntries).isEmpty()
        }

        @Test
        fun `should not affect other users' entries`() {
            // given
            val userId1 = 1L
            val userId2 = 2L
            val entries =
                listOf(
                    createQueueEntry(userId = userId1, status = QueueEntryStatus.WAITING),
                    createQueueEntry(userId = userId2, status = QueueEntryStatus.PROCESSING),
                )
            queueEntryRepository.saveAll(entries)

            // when
            queueService.dequeueExistingEntries(userId1)

            // then
            val remainingEntries = queueEntryRepository.findAllByUserIdWithStatus(userId2, QueueEntryStatus.PROCESSING)
            Assertions.assertThat(remainingEntries).hasSize(1)
        }
    }

    companion object {
        @Container
        private val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8").apply {
                withDatabaseName("queue_service_test_db")
                withUsername("testuser")
                withPassword("testpass")
                withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--default-time-zone=+00:00",
                )
                withEnv("TZ", "UTC")
                withReuse(true)
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                "${mysqlContainer.jdbcUrl}?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
            }
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
        }
    }
}
