package com.yuiyeong.ticketing.application.annotation

/**
 * Http header 에 User-Token 로 부터 추출한 userId 를 받음 나타내는 애노테이션
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUserId
