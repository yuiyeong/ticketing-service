package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.infrastructure.entity.ReservationEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl(
    private val reservationJpaRepository: ReservationJpaRepository,
) : ReservationRepository {
    override fun save(reservation: Reservation): Reservation {
        val entity = reservationJpaRepository.save(ReservationEntity.from(reservation))
        return entity.toReservation()
    }

    override fun findOneById(id: Long): Reservation? = reservationJpaRepository.findByIdOrNull(id)?.toReservation()

    override fun findOneByIdWithLock(id: Long): Reservation? = reservationJpaRepository.findOneWithLockById(id)?.toReservation()

    override fun deleteAll() = reservationJpaRepository.deleteAll()
}
