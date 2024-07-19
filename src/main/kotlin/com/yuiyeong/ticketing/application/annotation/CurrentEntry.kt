package com.yuiyeong.ticketing.application.annotation

/**
 * Http header 에 User-Token 로 부터 QueueEntry 를 추출함을 나타내는 annotation
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentEntry
