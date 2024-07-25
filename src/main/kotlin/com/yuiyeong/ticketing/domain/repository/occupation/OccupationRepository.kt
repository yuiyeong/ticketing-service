package com.yuiyeong.ticketing.domain.repository.occupation

import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import java.time.ZonedDateTime

interface OccupationRepository {
    fun save(occupation: Occupation): Occupation

    fun saveAll(occupations: List<Occupation>): List<Occupation>

    fun findAllByStatusAndExpiresAtBeforeWithLock(
        status: OccupationStatus,
        moment: ZonedDateTime,
    ): List<Occupation>

    fun findAll(): List<Occupation>

    fun findOneById(id: Long): Occupation?

    fun findOneByIdWithLock(id: Long): Occupation?

    fun deleteAll()
}
