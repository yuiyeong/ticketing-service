package com.yuiyeong.ticketing.config.swagger.annotation.common

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "User-Token",
    description = "대기열 진입 시 발급받은 토큰",
    required = true,
    `in` = ParameterIn.HEADER,
)
annotation class UserTokenHeader
