package com.yuiyeong.ticketing.domain.notification

import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent

/**
 * 결제 관련 정보에 대한 알림을 보내는 Service
 */
interface PaymentNotificationService {
    /**
     * 결제 결과에 대한 알림을 보내는 함수
     */
    fun notifyPaymentResult(event: PaymentEvent)
}
