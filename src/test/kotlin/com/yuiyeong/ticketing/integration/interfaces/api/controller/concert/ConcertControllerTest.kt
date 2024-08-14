package com.yuiyeong.ticketing.integration.interfaces.api.controller.concert

import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.helper.DBCleanUp
import com.yuiyeong.ticketing.helper.TestDataFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcertControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dbCleanUp: DBCleanUp

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @AfterAll
    fun afterAll() {
        dbCleanUp.execute()
    }

    /**
     * 모든 콘서트 목록을 내려주어야 한다.
     */
    @Test
    fun `should return 200 ok with all concerts`() {
        // given
        concertRepository.save(TestDataFactory.createConcert())

        // when
        val result =
            mockMvc.perform(
                get("/api/v1/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "test user token"),
            )

        // then
        result
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.list.length()").value(1))
            .andExpect(jsonPath("$.list[0].id").value("1"))
            .andExpect(jsonPath("$.list[0].title").value("Test Concert"))
            .andExpect(jsonPath("$.list[0].singer").value("Test Singer"))
            .andExpect(jsonPath("$.list[0].description").value("Test Description"))
    }
}
