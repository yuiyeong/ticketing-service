package com.yuiyeong.ticketing.infrastructure.jpa.entity.concert

import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "concert")
class ConcertEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val title: String,
    val singer: String,
    val description: String,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toConcert(): Concert =
        Concert(
            id = id,
            title = title,
            singer = singer,
            description = description,
        )

    companion object {
        fun from(concert: Concert): ConcertEntity =
            ConcertEntity(
                id = concert.id,
                title = concert.title,
                singer = concert.singer,
                description = concert.description,
            )
    }
}
