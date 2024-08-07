package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.config.CacheNames
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import com.yuiyeong.ticketing.domain.service.lock.DistributedLockService
import com.yuiyeong.ticketing.domain.service.lock.LockKeyGenerator
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class OccupySeatUseCaseImpl(
    private val concertService: ConcertService,
    private val occupationService: OccupationService,
    private val distributedLockService: DistributedLockService,
    private val lockKeyGenerator: LockKeyGenerator,
) : OccupySeatUseCase {
    @CacheEvict(value = [CacheNames.AVAILABLE_CONCERT_EVENTS], allEntries = true)
    override fun execute(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult {
        // 콘서트 이벤트가 예약 기간인지 확인
        val now = ZonedDateTime.now().asUtc
        val concertEvent = concertService.getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(now)

        // seatId 를 가지는 좌석에 대해 점유
        val seatIds = listOf(seatId)
        val occupation =
            distributedLockService.withLock(lockKeyGenerator.generateConcertEventSeatKey(concertEvent, seatId)) {
                occupationService.occupy(userId, concertEventId, seatIds)
            } ?: throw SeatUnavailableException()

        // 콘서트 이벤트의 선택 가능한 좌석 수 업데이트
        concertService.refreshAvailableSeats(concertEventId)
        return OccupationResult.from(occupation)
    }
}
