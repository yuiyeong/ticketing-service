package com.yuiyeong.ticketing.domain.repository.concert

import com.yuiyeong.ticketing.domain.model.concert.Concert

interface ConcertRepository {
    fun save(concert: Concert): Concert

    fun findAll(): List<Concert>
}
