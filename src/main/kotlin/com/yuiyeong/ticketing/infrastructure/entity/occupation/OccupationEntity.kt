package com.yuiyeong.ticketing.infrastructure.entity.occupation

import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.infrastructure.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
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
    val status: OccupationEntityStatus,
    val expiresAt: ZonedDateTime,
    val expiredAt: ZonedDateTime?,
    @OneToMany(mappedBy = "occupation", cascade = [CascadeType.ALL])
    val seatAllocations: List<SeatAllocationEntity> = listOf(),
) : BaseEntity() {
    fun toOccupation(): Occupation =
        Occupation(
            id = id,
            userId = userId,
            concertEventId = concertEventId,
            allocations = seatAllocations.map { it.toSeatAllocation() },
            status = status.toOccupationStatus(),
            createdAt = createdAt,
            expiresAt = expiresAt,
            expiredAt = expiredAt,
        )

    companion object {
        fun from(occupation: Occupation): OccupationEntity =
            OccupationEntity(
                id = occupation.id,
                userId = occupation.userId,
                concertEventId = occupation.concertEventId,
                status = OccupationEntityStatus.from(occupation.status),
                expiresAt = occupation.expiresAt,
                expiredAt = occupation.expiredAt,
                seatAllocations = occupation.allocations.map { SeatAllocationEntity.from(it) },
            )
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
