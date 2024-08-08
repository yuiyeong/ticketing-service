package com.yuiyeong.ticketing.infrastructure.jpa.entity.wallet

import com.yuiyeong.ticketing.domain.model.wallet.Wallet
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "wallet")
class WalletEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val balance: BigDecimal,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toWallet(): Wallet =
        Wallet(
            id = id,
            userId = userId,
            balance = balance,
            createdAt = auditable.createdAt,
            updatedAt = auditable.updatedAt,
        )

    companion object {
        fun from(wallet: Wallet): WalletEntity =
            WalletEntity(
                id = wallet.id,
                userId = wallet.userId,
                balance = wallet.balance,
            )
    }
}
