import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class QueueEntryRepositoryTest {
    @Autowired
    private lateinit var queueEntryRepository: QueueEntryRepository

    private lateinit var testEntries: List<QueueEntry>

    @BeforeEach
    fun setUp() {
        val now = ZonedDateTime.now().asUtc
        testEntries =
            listOf(
                createQueueEntry(1L, 1L, "token1", 1, QueueEntryStatus.WAITING, now),
                createQueueEntry(2L, 1L, "token2", 2, QueueEntryStatus.PROCESSING, now),
                createQueueEntry(3L, 2L, "token3", 3, QueueEntryStatus.WAITING, now),
                createQueueEntry(4L, 2L, "token4", 4, QueueEntryStatus.EXITED, now),
                createQueueEntry(5L, 3L, "token5", 5, QueueEntryStatus.EXPIRED, now),
            )
        queueEntryRepository.saveAll(testEntries)
    }

    @Test
    fun `should save and return queue entry`() {
        // given
        val now = ZonedDateTime.now().asUtc
        val newEntry = createQueueEntry(0L, 4L, "newToken", 6, QueueEntryStatus.WAITING, now)

        // when
        val savedEntry = queueEntryRepository.save(newEntry)

        // then
        Assertions.assertThat(savedEntry.id).isNotNull()
        Assertions.assertThat(savedEntry.token).isEqualTo("newToken")
        Assertions.assertThat(savedEntry.userId).isEqualTo(4L)
    }

    @Test
    fun `should find queue entry by token`() {
        // when
        val foundEntry = queueEntryRepository.findOneByToken("token1")

        // then
        Assertions.assertThat(foundEntry).isNotNull
        Assertions.assertThat(foundEntry?.userId).isEqualTo(1L)
        Assertions.assertThat(foundEntry?.status).isEqualTo(QueueEntryStatus.WAITING)
    }

    @Test
    fun `should find queue entry by id`() {
        // when
        val foundEntry = queueEntryRepository.findOneByIdWithLock(1L)

        // then
        Assertions.assertThat(foundEntry).isNotNull
        Assertions.assertThat(foundEntry?.token).isEqualTo("token1")
    }

    @Test
    fun `should find all entries by user id and status`() {
        // when
        val foundEntries =
            queueEntryRepository.findAllByUserIdWithStatus(1L, QueueEntryStatus.WAITING, QueueEntryStatus.PROCESSING)

        // then
        Assertions.assertThat(foundEntries).hasSize(2)
        Assertions.assertThat(foundEntries.map { it.token }).containsExactlyInAnyOrder("token1", "token2")
    }

    @Test
    fun `should find all entries by status`() {
        // when
        val foundEntries = queueEntryRepository.findAllByStatus(QueueEntryStatus.WAITING)

        // then
        Assertions.assertThat(foundEntries).hasSize(2)
        Assertions.assertThat(foundEntries.map { it.token }).containsExactlyInAnyOrder("token1", "token3")
    }

    @Test
    fun `should find entries by status ordered by position with limit`() {
        // when
        val foundEntries = queueEntryRepository.findAllByStatusOrderByPositionWithLock(QueueEntryStatus.WAITING, 1)

        // then
        Assertions.assertThat(foundEntries).hasSize(1)
        Assertions.assertThat(foundEntries.first().token).isEqualTo("token1")
    }

    @Test
    fun `should find entries that has expireAt before a given moment`() {
        // when
        val moment = ZonedDateTime.now().asUtc.plusHours(1)
        val foundEntries = queueEntryRepository.findAllByExpiresAtBeforeAndStatusWithLock(moment, QueueEntryStatus.WAITING)

        // then
        Assertions.assertThat(foundEntries).hasSize(2)
        Assertions.assertThat(foundEntries.first().token).isEqualTo("token1")
        Assertions.assertThat(foundEntries.last().token).isEqualTo("token3")
    }

    @Test
    fun `should find last waiting position`() {
        // when
        val lastPosition = queueEntryRepository.findLastWaitingPosition()

        // then
        Assertions.assertThat(lastPosition).isEqualTo(3L)
    }

    @Test
    fun `should find first waiting position`() {
        // when
        val firstPosition = queueEntryRepository.findFirstWaitingPosition()

        // then
        Assertions.assertThat(firstPosition).isEqualTo(1L)
    }

    private fun createQueueEntry(
        id: Long,
        userId: Long,
        token: String,
        position: Long,
        status: QueueEntryStatus,
        enteredAt: ZonedDateTime,
    ): QueueEntry =
        QueueEntry(
            id = id,
            userId = userId,
            token = token,
            position = position,
            status = status,
            expiresAt = enteredAt.plusMinutes(30),
            enteredAt = enteredAt,
            processingStartedAt = if (status == QueueEntryStatus.PROCESSING) enteredAt.plusMinutes(5) else null,
            exitedAt = if (status == QueueEntryStatus.EXITED) enteredAt.plusMinutes(10) else null,
            expiredAt = if (status == QueueEntryStatus.EXPIRED) enteredAt.plusMinutes(35) else null,
        )
}
