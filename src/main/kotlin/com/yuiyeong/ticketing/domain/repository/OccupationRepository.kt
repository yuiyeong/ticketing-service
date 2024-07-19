package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Occupation
import java.time.ZonedDateTime

interface OccupationRepository {
    fun save(occupation: Occupation): Occupation

    fun saveAll(occupations: List<Occupation>): List<Occupation>

    fun findAllByExpiresAtBeforeWithLock(moment: ZonedDateTime): List<Occupation>

    fun findOneById(id: Long): Occupation?

    fun findOneByIdWithLock(id: Long): Occupation?

    fun deleteAll()
}
