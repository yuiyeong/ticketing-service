package com.yuiyeong.ticketing.domain.repository.reservation

import com.yuiyeong.ticketing.domain.model.reservation.Reservation

interface ReservationRepository {
    fun save(reservation: Reservation): Reservation

    fun findOneById(id: Long): Reservation?

    fun findOneByIdWithLock(id: Long): Reservation?

    fun deleteAll()
}
