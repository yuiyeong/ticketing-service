package com.yuiyeong.ticketing.infrastructure.jpa.entity.occupation

import com.yuiyeong.ticketing.domain.model.occupation.AllocationStatus
import com.yuiyeong.ticketing.domain.model.occupation.SeatAllocation
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.ZonedDateTime

@Entity
@Table(name = "seat_allocation")
class SeatAllocationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val seatId: Long,
    val seatPrice: BigDecimal,
    val seatNumber: String,
    @Enumerated(EnumType.STRING)
    val status: SeatAllocationEntityStatus,
    val occupiedAt: ZonedDateTime?,
    val expiredAt: ZonedDateTime?,
    val reservedAt: ZonedDateTime?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val occupation: OccupationEntity? = null,
    val reservationId: Long? = null,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toSeatAllocation(): SeatAllocation =
        SeatAllocation(
            id = id,
            userId = userId,
            seatId = seatId,
            seatPrice = seatPrice,
            seatNumber = seatNumber,
            status = status.toAllocationStatus(),
            occupiedAt = occupiedAt,
            expiredAt = expiredAt,
            reservedAt = reservedAt,
        )

    companion object {
        fun create(
            seatAllocation: SeatAllocation,
            occupationEntity: OccupationEntity?,
        ): SeatAllocationEntity =
            SeatAllocationEntity(
                id = seatAllocation.id,
                userId = seatAllocation.userId,
                seatId = seatAllocation.seatId,
                seatPrice = seatAllocation.seatPrice,
                seatNumber = seatAllocation.seatNumber,
                status = SeatAllocationEntityStatus.from(seatAllocation.status),
                occupiedAt = seatAllocation.occupiedAt,
                expiredAt = seatAllocation.expiredAt,
                reservedAt = seatAllocation.reservedAt,
                occupation = occupationEntity,
                reservationId = occupationEntity?.reservationId,
            )
    }
}

enum class SeatAllocationEntityStatus {
    OCCUPIED,
    EXPIRED,
    RESERVED,
    ;

    fun toAllocationStatus(): AllocationStatus =
        when (this) {
            OCCUPIED -> AllocationStatus.OCCUPIED
            EXPIRED -> AllocationStatus.EXPIRED
            RESERVED -> AllocationStatus.RESERVED
        }

    companion object {
        fun from(allocationStatus: AllocationStatus): SeatAllocationEntityStatus =
            when (allocationStatus) {
                AllocationStatus.OCCUPIED -> OCCUPIED
                AllocationStatus.EXPIRED -> EXPIRED
                AllocationStatus.RESERVED -> RESERVED
            }
    }
}
