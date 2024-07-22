package com.yuiyeong.ticketing.infrastructure.entity.reservation

import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.infrastructure.entity.BaseEntity
import com.yuiyeong.ticketing.infrastructure.entity.occupation.SeatAllocationEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "reservation")
class ReservationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val concertId: Long,
    val concertEventId: Long,
    val status: ReservationEntityStatus,
    val totalSeats: Int,
    val totalAmount: BigDecimal,
    @OneToMany(mappedBy = "reservation")
    val seatAllocations: List<SeatAllocationEntity> = listOf(),
) : BaseEntity() {
    fun toReservation(): Reservation =
        Reservation(
            id = id,
            userId = userId,
            concertId = concertId,
            concertEventId = concertEventId,
            status = status.toReservationStatus(),
            totalSeats = totalSeats,
            totalAmount = totalAmount,
            createdAt = createdAt,
        )

    companion object {
        fun from(reservation: Reservation): ReservationEntity =
            ReservationEntity(
                id = reservation.id,
                userId = reservation.userId,
                concertId = reservation.concertId,
                concertEventId = reservation.concertEventId,
                status = ReservationEntityStatus.from(reservation.status),
                totalSeats = reservation.totalSeats,
                totalAmount = reservation.totalAmount,
            )
    }
}

enum class ReservationEntityStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    ;

    fun toReservationStatus(): ReservationStatus =
        when (this) {
            PENDING -> ReservationStatus.PENDING
            CONFIRMED -> ReservationStatus.CONFIRMED
            PAYMENT_FAILED -> ReservationStatus.PAYMENT_FAILED
        }

    companion object {
        fun from(reservationStatus: ReservationStatus): ReservationEntityStatus =
            when (reservationStatus) {
                ReservationStatus.PENDING -> PENDING
                ReservationStatus.CONFIRMED -> CONFIRMED
                ReservationStatus.PAYMENT_FAILED -> PAYMENT_FAILED
            }
    }
}
