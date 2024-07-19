package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.Concert
import com.yuiyeong.ticketing.domain.repository.ConcertRepository
import com.yuiyeong.ticketing.infrastructure.entity.ConcertEntity
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

    override fun deleteAll() = concertJpaRepository.deleteAll()
}
