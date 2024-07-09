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
class ConcertControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return available dates when concert exists`() {
        // given
        val concertId = 1L
        val validToken = "validQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts/$concertId/available-dates")
                    .header("User-Token", validToken),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.list[0].id").value(1))
            .andExpect(jsonPath("$.list[0].date").value("2024-07-07"))
    }

    @Test
    fun `should return empty list when no available dates`() {
        // given
        val concertId = 2L
        val validToken = "validQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts/$concertId/available-dates")
                    .header("User-Token", validToken),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.list").isEmpty)
    }

    @Test
    fun `should return 400 Bad Request when User-Token header is missing`() {
        // given
        val concertId = 1L

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts/$concertId/available-dates"),
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
        val concertId = 1L
        val invalidToken = "invalidQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts/$concertId/available-dates")
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
    fun `should return 404 Not Found when concert does not exist`() {
        // given
        val nonExistentConcertId = 999L
        val validToken = "validQueueToken"

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts/$nonExistentConcertId/available-dates")
                    .header("User-Token", validToken),
            )

        // then
        result
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("not_found_concert"))
            .andExpect(jsonPath("$.error.message").value("요청한 콘서트를 찾을 수 없습니다."))
    }
}
