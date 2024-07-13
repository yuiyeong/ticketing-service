package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationDto
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class OccupationUseCaseImpl(
    private val queueService: QueueService,
    private val occupationService: OccupationService,
) : OccupationUseCase {
    override fun occupySeat(
        userToken: String?,
        concertEventId: Long,
        seatId: Long,
    ): OccupationDto {
        val entry = queueService.verifyEntryOnProcessing(userToken)
        val occupation = occupationService.occupySeat(entry.userId, concertEventId, seatId)
        return OccupationDto.from(occupation)
    }
}
