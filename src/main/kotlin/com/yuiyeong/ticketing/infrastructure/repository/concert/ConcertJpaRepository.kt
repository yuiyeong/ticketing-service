package com.yuiyeong.ticketing.infrastructure.repository.concert

import com.yuiyeong.ticketing.infrastructure.entity.concert.ConcertEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long>
