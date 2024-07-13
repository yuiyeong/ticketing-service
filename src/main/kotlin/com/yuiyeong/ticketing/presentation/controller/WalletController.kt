package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.usecase.ChargingWalletUseCase
import com.yuiyeong.ticketing.application.usecase.WalletBalanceUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.ChargeWalletApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.WalletBalanceApiDoc
import com.yuiyeong.ticketing.presentation.dto.WalletDto
import com.yuiyeong.ticketing.presentation.dto.request.ChargingWalletRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "지갑", description = "지갑 관련 api")
class WalletController {
    @Autowired
    private lateinit var chargingWalletUseCase: ChargingWalletUseCase

    @Autowired
    private lateinit var walletBalanceUseCase: WalletBalanceUseCase

    @GetMapping("{userId}/wallet")
    @WalletBalanceApiDoc
    fun getBalance(
        @PathVariable("userId") userId: Long,
    ): TicketingResponse<WalletDto> {
        val data = WalletDto.from(walletBalanceUseCase.getBalance(userId))
        return TicketingResponse(data)
    }

    @PatchMapping("{userId}/wallet/charge")
    @ChargeWalletApiDoc
    fun charge(
        @PathVariable("userId") userId: Long,
        @RequestBody req: ChargingWalletRequest,
    ): TicketingResponse<WalletDto> {
        val data = WalletDto.from(chargingWalletUseCase.charge(userId, req.amount))
        return TicketingResponse(data)
    }
}
