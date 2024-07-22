package com.yuiyeong.ticketing.application.annotation

import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus

/**
 * Http header 에 User-Token 이 필수임을 나타내는 annotation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresUserToken(
    val allowedStatus: Array<QueueEntryStatus> = [QueueEntryStatus.PROCESSING],
)
