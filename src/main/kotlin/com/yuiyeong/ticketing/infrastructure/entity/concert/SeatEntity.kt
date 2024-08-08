package com.yuiyeong.ticketing.infrastructure.entity.concert

import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.infrastructure.entity.audit.Auditable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(
    name = "seat",
    indexes = [
        Index(
            name = "idx_concert_event_desc_available",
            columnList = "concert_event_id DESC, is_available",
        ),
    ],
)
class SeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val concertEventId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toSeat(): Seat =
        Seat(
            id = id,
            concertEventId = concertEventId,
            seatNumber = seatNumber,
            price = price,
            isAvailable = isAvailable,
        )

    companion object {
        fun from(seat: Seat): SeatEntity =
            SeatEntity(
                id = seat.id,
                concertEventId = seat.concertEventId,
                seatNumber = seat.seatNumber,
                price = seat.price,
                isAvailable = seat.isAvailable,
            )
    }
}
