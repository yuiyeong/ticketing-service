package com.yuiyeong.ticketing.infrastructure.entity

import com.yuiyeong.ticketing.domain.model.Wallet
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
) : BaseEntity() {
    fun toWallet(): Wallet =
        Wallet(
            id = this.id,
            userId = this.userId,
            balance = this.balance,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
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
