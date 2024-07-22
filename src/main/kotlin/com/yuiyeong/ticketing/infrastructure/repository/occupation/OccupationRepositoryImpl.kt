package com.yuiyeong.ticketing.infrastructure.repository.occupation

import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.infrastructure.entity.occupation.OccupationEntity
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

    override fun findAllByExpiresAtBeforeWithLock(moment: ZonedDateTime): List<Occupation> =
        occupationJpaRepository.findAllWithLockByExpiresAtBefore(moment).map {
            it.toOccupation()
        }

    override fun findOneById(id: Long): Occupation? = occupationJpaRepository.findOneById(id)?.toOccupation()

    override fun findOneByIdWithLock(id: Long): Occupation? = occupationJpaRepository.findOneWithLockById(id)?.toOccupation()

    override fun deleteAll() = occupationJpaRepository.deleteAll()
}
