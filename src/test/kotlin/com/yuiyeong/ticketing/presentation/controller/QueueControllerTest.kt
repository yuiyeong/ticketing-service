package com.yuiyeong.ticketing.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@AutoConfigureMockMvc
@SpringBootTest
class QueueControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return token and estimated waiting time when generating a waiting token`() {
        // given
        val userId = 1L
        val requestDto = QueueTokenRequestDto(userId)
        val requestBody = objectMapper.writeValueAsString(requestDto)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/queue/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .header("Authorization", "Bearer test_access_token"),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.token").value("validQueueToken"))
            .andExpect(jsonPath("$.data.estimatedWaitingTime").exists())
            .andExpect(jsonPath("$.data.estimatedWaitingTime").value(10))
    }

    @Test
    fun `should return queue status when valid token is provided`() {
        // given
        val validToken = "validQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/queue/status")
                    .header("User-Token", validToken),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.queuePosition").value(2))
            .andExpect(jsonPath("$.data.estimatedWaitingTime").value(20))
    }

    @Test
    fun `should return 400 Bad Request when User-Token header is missing`() {
        // when
        val result =
            mockMvc.perform(
                get("/api/v1/queue/status"),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("invalid_token"))
            .andExpect(jsonPath("$.error.message").value("유효하지 않은 token 입니다."))
    }

    @Test
    fun `should return 400 Bad Request when invalid token is provided`() {
        // given
        val invalidToken = "invalidQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/queue/status")
                    .header("User-Token", invalidToken),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("invalid_token"))
            .andExpect(jsonPath("$.error.message").value("유효하지 않은 token 입니다."))
    }

    @Test
    fun `should return 404 Not Found when token is not found in queue`() {
        // given
        val notInQueueToken = "notInQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/queue/status")
                    .header("User-Token", notInQueueToken),
            )

        // then
        result
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("not_found_in_queue"))
            .andExpect(jsonPath("$.error.message").value("해당 토큰으로 대기 중인 정보를 찾을 수 없습니다."))
    }

    data class QueueTokenRequestDto(
        val userId: Long,
    )
}
