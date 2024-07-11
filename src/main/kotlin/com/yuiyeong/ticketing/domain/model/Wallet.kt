package com.yuiyeong.ticketing.domain.model
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Wallet(
    val id: Long,
    val userId: Long,
    var balance: BigDecimal,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
