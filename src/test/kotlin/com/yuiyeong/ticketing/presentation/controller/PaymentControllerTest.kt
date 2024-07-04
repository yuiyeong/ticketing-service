package com.yuiyeong.ticketing.presentation.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest
class PaymentControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return payment history when user exists`() {
        // given
        val userId = 1L

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/users/$userId/payments"),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.list[0].id").value(21))
            .andExpect(jsonPath("$.list[0].reservationId").value(12))
            .andExpect(jsonPath("$.list[0].amount").value(60000))
            .andExpect(jsonPath("$.list[0].status").value("failed"))
            .andExpect(jsonPath("$.list[0].paidAt").value("2024-07-02T14:20:00Z"))
            .andExpect(jsonPath("$.list[1].id").value(22))
            .andExpect(jsonPath("$.list[1].reservationId").value(12))
            .andExpect(jsonPath("$.list[1].amount").value(60000))
            .andExpect(jsonPath("$.list[1].status").value("success"))
            .andExpect(jsonPath("$.list[1].paidAt").value("2024-07-02T14:25:00Z"))
    }

    @Test
    fun `should return empty list when user has no payment history`() {
        // given
        val userId = 2L

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/users/$userId/payments"),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.list").isEmpty)
    }
}
