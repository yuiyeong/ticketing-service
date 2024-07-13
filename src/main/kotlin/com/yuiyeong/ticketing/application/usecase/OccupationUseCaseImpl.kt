package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationResult
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
    ): OccupationResult {
        val entry = queueService.verifyEntryOnProcessing(userToken)
        val occupation = occupationService.occupySeat(entry.userId, concertEventId, seatId)
        return OccupationResult.from(occupation)
    }
}
