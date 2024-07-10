package com.yuiyeong.ticketing.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest
class WalletControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return user's balance when user exists`() {
        // given
        val userId = 1L

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/users/$userId/wallet"),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.balance").value(50000))
    }

    @Test
    fun `should charge wallet and return updated balance when valid amount is provided`() {
        // given
        val userId = 1L
        val chargeRequest = ChargeRequest(amount = 50000)

        // when
        val result =
            mockMvc.perform(
                patch("/api/v1/users/$userId/wallet/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.balance").value(50000))
    }

    @Test
    fun `should return 400 Bad Request when invalid amount is provided`() {
        // given
        val userId = 1L
        val invalidChargeRequest = ChargeRequest(amount = -1000)

        // when
        val result =
            mockMvc.perform(
                patch("/api/v1/users/$userId/wallet/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidChargeRequest)),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("invalid_amount"))
            .andExpect(jsonPath("$.error.message").value("유효하지 않은 충전 금액입니다."))
    }

    @Test
    fun `should return 404 Not Found when trying to charge non-existent user's wallet`() {
        // given
        val nonExistentUserId = 999L
        val chargeRequest = ChargeRequest(amount = 50000)

        // when
        val result =
            mockMvc.perform(
                patch("/api/v1/users/$nonExistentUserId/wallet/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)),
            )

        // then
        result
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("not_found_wallet"))
            .andExpect(jsonPath("$.error.message").value("요청한 사용자의 잔액을 찾을 수 없습니다."))
    }

    data class ChargeRequest(
        val amount: Int,
    )
}
