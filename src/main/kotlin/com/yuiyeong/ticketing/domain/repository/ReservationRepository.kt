package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Reservation

interface ReservationRepository {
    fun save(reservation: Reservation): Reservation

    fun findOneById(id: Long): Reservation?

    fun findOneByIdWithLock(id: Long): Reservation?

    fun deleteAll()
}
