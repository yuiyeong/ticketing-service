package com.yuiyeong.ticketing.interfaces.presentation.dto.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertResult

data class ConcertResponseDto(
    val id: Long,
    val title: String,
    val singer: String,
    val description: String,
) {
    companion object {
        fun from(result: ConcertResult): ConcertResponseDto =
            ConcertResponseDto(
                id = result.id,
                title = result.title,
                singer = result.singer,
                description = result.description,
            )
    }
}
