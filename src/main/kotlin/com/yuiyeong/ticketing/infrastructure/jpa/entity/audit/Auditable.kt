package com.yuiyeong.ticketing.infrastructure.jpa.entity.audit

import com.yuiyeong.ticketing.common.asUtc
import jakarta.persistence.Embeddable
import jakarta.persistence.EntityListeners
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@EntityListeners(value = [AuditingEntityListener::class])
@Embeddable
class Auditable {
    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now().asUtc

    @LastModifiedDate
    val updatedAt: ZonedDateTime = ZonedDateTime.now().asUtc
}
