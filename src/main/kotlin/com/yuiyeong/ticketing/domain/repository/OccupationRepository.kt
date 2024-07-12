package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Occupation
import java.time.ZonedDateTime

interface OccupationRepository {
    fun save(occupation: Occupation): Occupation

    fun saveAll(occupations: List<Occupation>): List<Occupation>

    fun findAllByExpiresAtBefore(moment: ZonedDateTime): List<Occupation>
}
