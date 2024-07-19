package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.infrastructure.entity.ConcertEventEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
class ConcertEventRepositoryImpl(
    private val concertEventJpaRepository: ConcertEventJpaRepository,
) : ConcertEventRepository {
    override fun save(concertEvent: ConcertEvent): ConcertEvent {
        val entity = concertEventJpaRepository.save(ConcertEventEntity.from(concertEvent))
        return entity.toConcertEvent()
    }

    override fun saveAll(concertEvents: List<ConcertEvent>): List<ConcertEvent> {
        val entities = concertEventJpaRepository.saveAll(concertEvents.map { ConcertEventEntity.from(it) })
        return entities.map { it.toConcertEvent() }
    }

    override fun findAllWithinPeriodBy(
        concertId: Long,
        moment: ZonedDateTime,
    ): List<ConcertEvent> =
        concertEventJpaRepository
            .findAllWithinPeriodByConcertId(
                concertId,
                moment,
            ).map { it.toConcertEvent() }

    override fun findOneById(id: Long): ConcertEvent? = concertEventJpaRepository.findByIdOrNull(id)?.toConcertEvent()

    override fun findOneByIdWithLock(id: Long): ConcertEvent? = concertEventJpaRepository.findOneWithLockById(id)?.toConcertEvent()

    override fun deleteAll() = concertEventJpaRepository.deleteAll()
}
