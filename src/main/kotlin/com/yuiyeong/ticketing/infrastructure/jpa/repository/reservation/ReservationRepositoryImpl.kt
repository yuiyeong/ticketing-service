package com.yuiyeong.ticketing.infrastructure.jpa.repository.reservation

import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.infrastructure.jpa.entity.reservation.ReservationEntity
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
}
