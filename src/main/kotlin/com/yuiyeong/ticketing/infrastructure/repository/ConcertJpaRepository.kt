package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.ConcertEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long>
