package com.yuiyeong.ticketing.unit.domain.model

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExitedException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyProcessingException
import com.yuiyeong.ticketing.domain.exception.QueueEntryOverdueException
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import java.time.ZonedDateTime
import kotlin.test.Test

class QueueEntryTest {
    @Test
    fun `should return estimated waiting time about waiting position`() {
        // given
        val positionOffset = 0L
        val estimatedTimeUnit = QueueEntry.WORKING_MINUTES

        val firstPosition = 1L
        val firstEntry = createQueueEntry(firstPosition)

        val secondPosition = 2L
        val secondEntry = createQueueEntry(secondPosition)

        val thirdPosition = 3L
        val thirdEntry = createQueueEntry(thirdPosition)

        // when
        val firstEstimatedTime = firstEntry.calculateEstimatedWaitingTime(positionOffset)
        val secondEstimatedTime = secondEntry.calculateEstimatedWaitingTime(positionOffset)
        val thirdEstimatedTime = thirdEntry.calculateEstimatedWaitingTime(positionOffset)

        // then
        Assertions.assertThat(firstEstimatedTime).isEqualTo(firstPosition * estimatedTimeUnit)
        Assertions.assertThat(secondEstimatedTime).isEqualTo(secondPosition * estimatedTimeUnit)
        Assertions.assertThat(thirdEstimatedTime).isEqualTo(thirdPosition * estimatedTimeUnit)
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should change status into PROCESSING`() {
            // given
            val queueEntry = createQueueEntry(1L)
            val now = ZonedDateTime.now().asUtc

            // when
            queueEntry.process(now)

            // then
            Assertions.assertThat(queueEntry.status).isEqualTo(QueueEntryStatus.PROCESSING)
            Assertions.assertThat(queueEntry.position).isEqualTo(0)
            Assertions.assertThat(queueEntry.processingStartedAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to process about an expired queueEntry`() {
            // given
            val expiredEntry = createQueueEntry(1L, QueueEntryStatus.EXPIRED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to process about an exited queueEntry`() {
            // given
            val exitedEntry = createQueueEntry(1L, QueueEntryStatus.EXITED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }

        @Test
        fun `should throw QueueEntryOverdueException when trying to process about an overdue queueEntry`() {
            // given
            val queueEntry = createQueueEntry(1L, QueueEntryStatus.WAITING)
            val overExpiredAt = queueEntry.expiresAt.plusSeconds(1)

            // when & then
            Assertions
                .assertThatThrownBy { queueEntry.process(overExpiredAt) }
                .isInstanceOf(QueueEntryOverdueException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyProcessingException when trying to process about an processing queueEntry`() {
            // given
            val queueEntry = createQueueEntry(1L, QueueEntryStatus.PROCESSING)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { queueEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyProcessingException::class.java)
        }
    }

    @Nested
    inner class ExitTest {
        @Test
        fun `should change status to EXITED`() {
            // given
            val queueEntry = createQueueEntry(3L)
            val now = ZonedDateTime.now().asUtc.plusMinutes(2)

            // when
            queueEntry.exit(now)

            // then
            Assertions.assertThat(queueEntry.status).isEqualTo(QueueEntryStatus.EXITED)
            Assertions.assertThat(queueEntry.position).isEqualTo(0)
            Assertions.assertThat(queueEntry.exitedAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to exit about an expired queueEntry`() {
            // given
            val expiredEntry = createQueueEntry(1L, QueueEntryStatus.EXPIRED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.exit(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to exit about an exited queueEntry`() {
            // given
            val exitedEntry = createQueueEntry(1L, QueueEntryStatus.EXITED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.exit(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }

        @Test
        fun `should throw QueueEntryOverdueException when trying to exit about an overdue queueEntry`() {
            // given
            val queueEntry = createQueueEntry(1L, QueueEntryStatus.WAITING)
            val overExpiredAt = queueEntry.expiresAt.plusSeconds(1)

            // when & then
            Assertions
                .assertThatThrownBy { queueEntry.exit(overExpiredAt) }
                .isInstanceOf(QueueEntryOverdueException::class.java)
        }
    }

    @Nested
    inner class ExpireTest {
        @Test
        fun `should change status to EXPIRED`() {
            // given
            val queueEntry = createQueueEntry(31L)
            val now = ZonedDateTime.now().asUtc.plusSeconds(22)

            // when
            queueEntry.expire(now)

            // then
            Assertions.assertThat(queueEntry.status).isEqualTo(QueueEntryStatus.EXPIRED)
            Assertions.assertThat(queueEntry.position).isEqualTo(0)
            Assertions.assertThat(queueEntry.expiredAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to expire about an expired queueEntry`() {
            // given
            val expiredEntry = createQueueEntry(1L, QueueEntryStatus.EXPIRED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.expire(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to expire about an exited queueEntry`() {
            // given
            val exitedEntry = createQueueEntry(1L, QueueEntryStatus.EXITED)
            val now = ZonedDateTime.now().asUtc

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.expire(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }
    }

    private fun createQueueEntry(
        position: Long,
        status: QueueEntryStatus = QueueEntryStatus.WAITING,
        createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    ) = QueueEntry(
        id = 123L,
        userId = 332L,
        token = "hello_test_token",
        position = position,
        status = status,
        expiresAt = createdAt.plusMinutes(10),
        enteredAt = createdAt,
        processingStartedAt = null,
        exitedAt = null,
        expiredAt = null,
    )
}
