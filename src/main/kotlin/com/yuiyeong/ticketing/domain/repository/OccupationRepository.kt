package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Occupation

interface OccupationRepository {
    fun save(occupation: Occupation): Occupation
}
