package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.queue")
class QueueProperties {
    var tokenTtlInSeconds: Long = 60L * 60L // token 의 ttl 을 초 단위로; 60 분
    var maxCountToMove: Int = 1000 // 활성화 시킬 토큰 수
    var batchSizeToMoveToActive: Int = 500 // waiting token 에서 active token 으로 변경할 때 batch size
    var activeRate: Long = 10 * 1000L // 활성화 주기; 10초
    var estimatedWorkingTimeInMinutes: Long = 10L // 작업 시간을 분 단위로; 10분
}
