package com.yuiyeong.ticketing.infrastructure.slack

import com.slack.api.webhook.Payload
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import org.springframework.stereotype.Component

@Component
class SlackPayloadCreator {
    fun createPayloadFrom(paymentMessage: PaymentMessage): Payload =
        Payload
            .builder()
            .text(
                buildString {
                    appendLine("<새로운 결제가 완료되었습니다>")
                    appendLine("사용자 ID: ${paymentMessage.userId}")
                    appendLine("예약 ID: ${paymentMessage.reservationId}")
                    if (paymentMessage is PaymentMessage.Failure) {
                        appendLine("결제 상태: 실패")
                        appendLine("실패 사유: ${paymentMessage.failureReason}")
                    } else {
                        appendLine("결제 상태: 성공")
                    }
                    appendLine("결제 금액: ${paymentMessage.amount}")
                    appendLine("-----------------")
                },
            ).build()
}
