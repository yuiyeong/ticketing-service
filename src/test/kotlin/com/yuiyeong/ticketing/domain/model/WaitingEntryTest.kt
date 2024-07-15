package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExitedException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyProcessingException
import com.yuiyeong.ticketing.domain.exception.QueueEntryOverdueException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import java.time.ZonedDateTime
import kotlin.test.Test

class WaitingEntryTest {
    @Test
    fun `should return estimated waiting time about waiting position`() {
        // given
        val positionOffset = 0L
        val estimatedTimeUnit = WaitingEntry.WORKING_MINUTES

        val firstPosition = 1L
        val firstEntry = createWaitingEntry(firstPosition)

        val secondPosition = 2L
        val secondEntry = createWaitingEntry(secondPosition)

        val thirdPosition = 3L
        val thirdEntry = createWaitingEntry(thirdPosition)

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
            val waitingEntry = createWaitingEntry(1L)
            val now = ZonedDateTime.now()

            // when
            waitingEntry.process(now)

            // then
            Assertions.assertThat(waitingEntry.status).isEqualTo(WaitingEntryStatus.PROCESSING)
            Assertions.assertThat(waitingEntry.position).isEqualTo(0)
            Assertions.assertThat(waitingEntry.processingStartedAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to process about an expired WaitingEntry`() {
            // given
            val expiredEntry = createWaitingEntry(1L, WaitingEntryStatus.EXPIRED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to process about an exited WaitingEntry`() {
            // given
            val exitedEntry = createWaitingEntry(1L, WaitingEntryStatus.EXITED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }

        @Test
        fun `should throw QueueEntryOverdueException when trying to process about an overdue WaitingEntry`() {
            // given
            val waitingEntry = createWaitingEntry(1L, WaitingEntryStatus.WAITING)
            val overExpiredAt = waitingEntry.expiresAt.plusSeconds(1)

            // when & then
            Assertions
                .assertThatThrownBy { waitingEntry.process(overExpiredAt) }
                .isInstanceOf(QueueEntryOverdueException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyProcessingException when trying to process about an processing WaitingEntry`() {
            // given
            val waitingEntry = createWaitingEntry(1L, WaitingEntryStatus.PROCESSING)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { waitingEntry.process(now) }
                .isInstanceOf(QueueEntryAlreadyProcessingException::class.java)
        }
    }

    @Nested
    inner class ExitTest {
        @Test
        fun `should change status to EXITED`() {
            // given
            val waitingEntry = createWaitingEntry(3L)
            val now = ZonedDateTime.now().plusMinutes(2)

            // when
            waitingEntry.exit(now)

            // then
            Assertions.assertThat(waitingEntry.status).isEqualTo(WaitingEntryStatus.EXITED)
            Assertions.assertThat(waitingEntry.position).isEqualTo(0)
            Assertions.assertThat(waitingEntry.exitedAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to exit about an expired WaitingEntry`() {
            // given
            val expiredEntry = createWaitingEntry(1L, WaitingEntryStatus.EXPIRED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.exit(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to exit about an exited WaitingEntry`() {
            // given
            val exitedEntry = createWaitingEntry(1L, WaitingEntryStatus.EXITED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.exit(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }

        @Test
        fun `should throw QueueEntryOverdueException when trying to exit about an overdue WaitingEntry`() {
            // given
            val waitingEntry = createWaitingEntry(1L, WaitingEntryStatus.WAITING)
            val overExpiredAt = waitingEntry.expiresAt.plusSeconds(1)

            // when & then
            Assertions
                .assertThatThrownBy { waitingEntry.exit(overExpiredAt) }
                .isInstanceOf(QueueEntryOverdueException::class.java)
        }
    }

    @Nested
    inner class ExpireTest {
        @Test
        fun `should change status to EXPIRED`() {
            // given
            val waitingEntry = createWaitingEntry(31L)
            val now = ZonedDateTime.now().plusSeconds(22)

            // when
            waitingEntry.expire(now)

            // then
            Assertions.assertThat(waitingEntry.status).isEqualTo(WaitingEntryStatus.EXPIRED)
            Assertions.assertThat(waitingEntry.position).isEqualTo(0)
            Assertions.assertThat(waitingEntry.expiredAt).isEqualTo(now)
        }

        @Test
        fun `should throw QueueEntryAlreadyExpiredException when trying to expire about an expired WaitingEntry`() {
            // given
            val expiredEntry = createWaitingEntry(1L, WaitingEntryStatus.EXPIRED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { expiredEntry.expire(now) }
                .isInstanceOf(QueueEntryAlreadyExpiredException::class.java)
        }

        @Test
        fun `should throw QueueEntryAlreadyExitedException when trying to expire about an exited WaitingEntry`() {
            // given
            val exitedEntry = createWaitingEntry(1L, WaitingEntryStatus.EXITED)
            val now = ZonedDateTime.now()

            // when & then
            Assertions
                .assertThatThrownBy { exitedEntry.expire(now) }
                .isInstanceOf(QueueEntryAlreadyExitedException::class.java)
        }
    }

    private fun createWaitingEntry(
        position: Long,
        status: WaitingEntryStatus = WaitingEntryStatus.WAITING,
        createdAt: ZonedDateTime = ZonedDateTime.now(),
    ) = WaitingEntry(
        id = 123L,
        userId = 332L,
        token = "hello_test_token",
        position = position,
        status = status,
        expiresAt = createdAt.plusMinutes(WaitingEntry.EXPIRATION_MINUTES),
        enteredAt = createdAt,
        processingStartedAt = null,
        exitedAt = null,
        expiredAt = null,
    )
}
