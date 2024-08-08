package com.yuiyeong.ticketing.infrastructure.slack

import com.slack.api.webhook.Payload
import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent
import org.springframework.stereotype.Component

@Component
class SlackPayloadCreator {
    fun createPayloadFrom(paymentEvent: PaymentEvent): Payload =
        Payload
            .builder()
            .text(
                buildString {
                    appendLine("<새로운 결제 완료되었습니다>")
                    appendLine("사용자 ID: ${paymentEvent.userId}")
                    appendLine("예약 ID: ${paymentEvent.reservationId}")
                    if (paymentEvent.failureReason != null) {
                        appendLine("결제 상태: 실패")
                        appendLine("실패 사유: ${paymentEvent.failureReason}")
                    } else {
                        appendLine("결제 상태: 성공")
                        appendLine("결제 금액: ${paymentEvent.transaction!!.amount}")
                    }
                    appendLine("-----------------")
                },
            ).build()
}
