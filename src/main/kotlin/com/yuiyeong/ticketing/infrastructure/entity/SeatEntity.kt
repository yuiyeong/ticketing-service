package com.yuiyeong.ticketing.infrastructure.entity

import com.yuiyeong.ticketing.domain.model.Seat
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "seat")
class SeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val concertEventId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
) : BaseEntity() {
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
