package com.yuiyeong.ticketing.application.dto.concert

import com.yuiyeong.ticketing.domain.model.concert.Concert

data class ConcertResult(
    val id: Long,
    val title: String,
    val singer: String,
    val description: String,
) {
    companion object {
        fun from(concert: Concert): ConcertResult =
            ConcertResult(
                id = concert.id,
                title = concert.title,
                singer = concert.singer,
                description = concert.description,
            )
    }
}
