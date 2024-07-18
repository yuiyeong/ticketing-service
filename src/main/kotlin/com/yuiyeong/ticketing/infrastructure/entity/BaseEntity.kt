package com.yuiyeong.ticketing.infrastructure.entity

import com.yuiyeong.ticketing.common.asUtc
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@MappedSuperclass
@EntityListeners(value = [AuditingEntityListener::class])
abstract class BaseEntity(
    @Column(nullable = false, updatable = false)
    var createdAt: ZonedDateTime = ZonedDateTime.now().asUtc,
    @Column(nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now().asUtc,
) {
    @PrePersist
    fun prePersist() {
        val now = ZonedDateTime.now().asUtc
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = ZonedDateTime.now().asUtc
    }
}
