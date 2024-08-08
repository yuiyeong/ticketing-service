package com.yuiyeong.ticketing.infrastructure.jpa.entity.occupation

import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.CascadeType
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "occupation")
class OccupationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val concertEventId: Long,
    val reservationId: Long?,
    @Enumerated(EnumType.STRING)
    val status: OccupationEntityStatus,
    val expiresAt: ZonedDateTime,
    val expiredAt: ZonedDateTime?,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    @OneToMany(mappedBy = "occupation", cascade = [CascadeType.ALL])
    var seatAllocations: List<SeatAllocationEntity> = listOf()
        private set

    fun toOccupation(): Occupation =
        Occupation(
            id = id,
            userId = userId,
            concertEventId = concertEventId,
            reservationId = reservationId,
            allocations = seatAllocations.map { it.toSeatAllocation() },
            status = status.toOccupationStatus(),
            createdAt = auditable.createdAt,
            expiresAt = expiresAt,
            expiredAt = expiredAt,
        )

    companion object {
        fun from(occupation: Occupation): OccupationEntity =
            OccupationEntity(
                id = occupation.id,
                userId = occupation.userId,
                concertEventId = occupation.concertEventId,
                reservationId = occupation.reservationId,
                status = OccupationEntityStatus.from(occupation.status),
                expiresAt = occupation.expiresAt,
                expiredAt = occupation.expiredAt,
            ).apply {
                seatAllocations =
                    occupation.allocations.map {
                        SeatAllocationEntity.create(it, this@apply)
                    }
            }
    }
}

enum class OccupationEntityStatus {
    ACTIVE,
    RELEASED,
    EXPIRED,
    ;

    fun toOccupationStatus(): OccupationStatus =
        when (this) {
            ACTIVE -> OccupationStatus.ACTIVE
            RELEASED -> OccupationStatus.RELEASED
            EXPIRED -> OccupationStatus.EXPIRED
        }

    companion object {
        fun from(occupationStatus: OccupationStatus): OccupationEntityStatus =
            when (occupationStatus) {
                OccupationStatus.ACTIVE -> ACTIVE
                OccupationStatus.RELEASED -> RELEASED
                OccupationStatus.EXPIRED -> EXPIRED
            }
    }
}
