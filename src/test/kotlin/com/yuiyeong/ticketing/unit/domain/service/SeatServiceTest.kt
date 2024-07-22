package com.yuiyeong.ticketing.unit.domain.service

import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.service.concert.SeatService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.math.BigDecimal
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class SeatServiceTest {
    @Mock
    private lateinit var seatRepository: SeatRepository

    private lateinit var seatService: SeatService

    @BeforeEach
    fun beforeEach() {
        seatService = SeatService(seatRepository)
    }

    @Test
    fun `should occupy seats that is passed by seatIds`() {
        // given
        val seats = listOf(createSeat(23L), createSeat(54L))
        val seatIds = seats.map { it.id }
        given(seatRepository.findAllAvailableByIds(seatIds)).willReturn(seats)

        given(seatRepository.saveAll(any())).willAnswer { invocation ->
            invocation.getArgument<List<Seat>>(0)
        }

        // when
        val occupiedSeats = seatService.occupy(seatIds)

        // then
        Assertions.assertThat(occupiedSeats.count()).isEqualTo(2)
        Assertions.assertThat(occupiedSeats[0].isAvailable).isEqualTo(false)
        Assertions.assertThat(occupiedSeats[1].isAvailable).isEqualTo(false)

        verify(seatRepository).findAllAvailableByIds(seatIds)
        verify(seatRepository).saveAll(
            argThat { it ->
                it[0].id == seatIds[0] && it[1].id == seatIds[1]
            },
        )
    }

    @Test
    fun `should throw SeatUnavailableException when trying to occupy a seat for another user`() {
        // given
        val seats = listOf(createSeat(23L))
        val seatIds = listOf(23L, 54L)
        given(seatRepository.findAllAvailableByIds(seatIds)).willReturn(seats)

        // when & then
        Assertions
            .assertThatThrownBy { seatService.occupy(seatIds) }
            .isInstanceOf(SeatUnavailableException::class.java)
        verify(seatRepository).findAllAvailableByIds(seatIds)
    }

    @Test
    fun `should throw SeatUnavailableException when trying to occupy seats for another user`() {
        val seatIds = listOf(23L, 54L)
        given(seatRepository.findAllAvailableByIds(seatIds)).willReturn(emptyList())

        // when & then
        Assertions
            .assertThatThrownBy { seatService.occupy(seatIds) }
            .isInstanceOf(SeatUnavailableException::class.java)
        verify(seatRepository).findAllAvailableByIds(seatIds)
    }

    private fun createSeat(id: Long) =
        Seat(
            id = id,
            concertEventId = 21,
            seatNumber = "$id 번",
            price = BigDecimal(1000),
            isAvailable = true,
        )
}
