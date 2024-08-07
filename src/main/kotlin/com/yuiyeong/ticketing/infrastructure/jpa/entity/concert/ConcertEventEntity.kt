package com.yuiyeong.ticketing.infrastructure.jpa.entity.concert

import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(
    name = "concert_event",
    indexes = [
        Index(
            name = "idx_concert_reservation_date_desc",
            columnList = "concert_id, reservation_start_at DESC, reservation_end_at DESC",
        ),
    ],
)
class ConcertEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val concert: ConcertEntity,
    val venue: String,
    val reservationStartAt: ZonedDateTime,
    val reservationEndAt: ZonedDateTime,
    val startAt: ZonedDateTime,
    val duration: Long,
    val maxSeatCount: Int,
    val availableSeatCount: Int,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toConcertEvent(): ConcertEvent =
        ConcertEvent(
            id = id,
            concert = concert.toConcert(),
            venue = venue,
            reservationPeriod = DateTimeRange(reservationStartAt, reservationEndAt),
            performanceSchedule = DateTimeRange(startAt, startAt.plusMinutes(duration)),
            maxSeatCount = maxSeatCount,
            availableSeatCount = availableSeatCount,
        )

    companion object {
        fun from(concertEvent: ConcertEvent): ConcertEventEntity =
            ConcertEventEntity(
                id = concertEvent.id,
                concert = ConcertEntity.from(concertEvent.concert),
                venue = concertEvent.venue,
                reservationStartAt = concertEvent.reservationPeriod.start,
                reservationEndAt = concertEvent.reservationPeriod.end,
                startAt = concertEvent.performanceSchedule.start,
                duration = concertEvent.performanceSchedule.getDurationAsMin(),
                maxSeatCount = concertEvent.maxSeatCount,
                availableSeatCount = concertEvent.availableSeatCount,
            )
    }
}
