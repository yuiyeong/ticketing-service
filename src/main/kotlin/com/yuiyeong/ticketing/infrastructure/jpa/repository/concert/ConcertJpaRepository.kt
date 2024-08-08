package com.yuiyeong.ticketing.infrastructure.jpa.repository.concert

import com.yuiyeong.ticketing.infrastructure.jpa.entity.concert.ConcertEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long>
