package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface QueueUseCase {
    /**
     * 사용자를 대기열에 입장시킵니다.
     *
     * @param userId 입장할 사용자의 ID
     * @return 생성된 WaitingEntry
     */
    fun enter(userId: Long): WaitingEntryDto

    /**
     * 주어진 토큰에 해당하는 사용자를 대기열에서 퇴장시킵니다.
     *
     * @param token 퇴장할 사용자의 토큰
     * @return 퇴장한 WaitingEntry
     * @throws InvalidTokenException 해당 토큰의 대기열 항목이 없을 경우
     */
    fun exit(token: String): WaitingEntryDto

    /**
     * 주어진 토큰에 해당하는 대기열 항목 정보를 조회합니다.
     *
     * @param token 조회할 대기열 항목의 토큰
     * @return 조회된 WaitingEntry
     * @throws InvalidTokenException 해당 토큰의 대기열 항목이 없을 경우
     */
    fun getEntryInfo(token: String): WaitingEntryDto

    /**
     * WAITING 상태인 대기열 항목 중 작업 가능 수만큼 PROCESSING 상태로 변경합니다.
     *
     * @return 활성화된 WaitingEntry 목록
     */
    fun activateWaitingEntries(): List<WaitingEntryDto>

    /**
     * 만료 시간이 지난 대기열 항목들을 EXPIRED 상태로 변경합니다.
     *
     * @return 만료 처리된 WaitingEntry 목록
     */
    fun expireOverdueEntries(): List<WaitingEntryDto>
}
