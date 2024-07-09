package com.yuiyeong.ticketing.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest
class ConcertEventControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should successfully reserve a seat when all conditions are met`() {
        // given
        val concertEventId = 1L
        val validToken = "validQueueToken"
        val reservationRequest = ReservationRequest(1234)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/concert-events/$concertEventId/reserve")
                    .header("User-Token", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservationRequest)),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.concertEventId").value(concertEventId))
            .andExpect(jsonPath("$.data.totalSeats").value(1))
            .andExpect(jsonPath("$.data.totalAmount").value(50000))
            .andExpect(jsonPath("$.data.createdAt").value("2024-07-01T12:05:00Z"))
    }

    @Test
    fun `should return 400 Bad Request when User-Token header is missing`() {
        // given
        val concertEventId = 1L
        val reservationRequest = ReservationRequest(1234)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/concert-events/$concertEventId/reserve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservationRequest)),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("invalid_token"))
            .andExpect(jsonPath("$.error.message").value("유효하지 않은 token 입니다."))
    }

    @Test
    fun `should return 400 Bad Request when seat is already occupied or reserved`() {
        // given
        val concertEventId = 1L
        val validToken = "validQueueToken"
        val occupiedSeatId = 5678L
        val reservationRequest = ReservationRequest(occupiedSeatId)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/concert-events/$concertEventId/reserve")
                    .header("User-Token", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservationRequest)),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("invalid_seat_status"))
            .andExpect(jsonPath("$.error.message").value("다른 사용자가 선택한 좌석이거나 이미 예약된 좌석입니다."))
    }

    @Test
    fun `should return 400 Bad Request when user has insufficient balance`() {
        // given
        val concertEventId = 1L
        val validToken = "validQueueToken"
        val expensiveSeatId = 9999L
        val reservationRequest = ReservationRequest(expensiveSeatId)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/concert-events/$concertEventId/reserve")
                    .header("User-Token", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservationRequest)),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("insufficient_balance"))
            .andExpect(jsonPath("$.error.message").value("잔액이 부족합니다."))
    }

    @Test
    fun `should return 400 Bad Request when seat occupation has expired`() {
        // given
        val concertEventId = 1L
        val validToken = "validQueueToken"
        val expiredSeatId = 1111L
        val reservationRequest = ReservationRequest(expiredSeatId)

        // when
        val result =
            mockMvc.perform(
                post("/api/v1/concert-events/$concertEventId/reserve")
                    .header("User-Token", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reservationRequest)),
            )

        // then
        result
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error.code").value("occupation_expired"))
            .andExpect(jsonPath("$.error.message").value("좌석 선택이 만료되었습니다."))
    }
}

data class ReservationRequest(
    val seatId: Long,
)
