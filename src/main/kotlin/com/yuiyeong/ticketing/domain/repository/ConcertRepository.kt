package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Concert

interface ConcertRepository {
    fun save(concert: Concert): Concert

    fun findAll(): List<Concert>

    fun deleteAll()
}
