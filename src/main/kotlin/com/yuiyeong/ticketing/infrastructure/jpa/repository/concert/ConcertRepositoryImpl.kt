package com.yuiyeong.ticketing.infrastructure.jpa.repository.concert

import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.infrastructure.jpa.entity.concert.ConcertEntity
import org.springframework.stereotype.Repository

@Repository
class ConcertRepositoryImpl(
    private val concertJpaRepository: ConcertJpaRepository,
) : ConcertRepository {
    override fun save(concert: Concert): Concert {
        val concertEntity = concertJpaRepository.save(ConcertEntity.from(concert))
        return concertEntity.toConcert()
    }

    override fun findAll(): List<Concert> = concertJpaRepository.findAll().map { it.toConcert() }
}
