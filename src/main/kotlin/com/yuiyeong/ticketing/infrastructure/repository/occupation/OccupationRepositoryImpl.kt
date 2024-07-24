package com.yuiyeong.ticketing.infrastructure.repository.occupation

import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.infrastructure.entity.occupation.OccupationEntity
import com.yuiyeong.ticketing.infrastructure.entity.occupation.OccupationEntityStatus
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
class OccupationRepositoryImpl(
    private val occupationJpaRepository: OccupationJpaRepository,
) : OccupationRepository {
    override fun save(occupation: Occupation): Occupation {
        val occupationEntity = occupationJpaRepository.save(OccupationEntity.from(occupation))
        return occupationEntity.toOccupation()
    }

    override fun saveAll(occupations: List<Occupation>): List<Occupation> {
        val occupationEntities = occupations.map { OccupationEntity.from(it) }
        return occupationJpaRepository.saveAll(occupationEntities).map { it.toOccupation() }
    }

    override fun findAllByStatusAndExpiresAtBeforeWithLock(
        status: OccupationStatus,
        moment: ZonedDateTime,
    ): List<Occupation> =
        occupationJpaRepository
            .findAllWithLockByStatusAndExpiresAtBefore(
                OccupationEntityStatus.from(status),
                moment,
            ).map {
                it.toOccupation()
            }

    override fun findAll(): List<Occupation> = occupationJpaRepository.findAll().map { it.toOccupation() }

    override fun findOneById(id: Long): Occupation? = occupationJpaRepository.findOneById(id)?.toOccupation()

    override fun findOneByIdWithLock(id: Long): Occupation? = occupationJpaRepository.findOneWithLockById(id)?.toOccupation()

    override fun deleteAll() = occupationJpaRepository.deleteAll()
}
