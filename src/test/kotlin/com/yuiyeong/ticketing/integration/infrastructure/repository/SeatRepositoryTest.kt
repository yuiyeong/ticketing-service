package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.Test

@SpringBootTest
@Transactional
class SeatRepositoryTest {
    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Test
    fun `should return a seat that has new id after saving it`() {
        // given
        val seat = createSeat(31L, true)

        // when
        val savedOne = seatRepository.save(seat)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(seat.id)
        Assertions.assertThat(savedOne.concertEventId).isEqualTo(seat.concertEventId)
        Assertions.assertThat(savedOne.seatNumber).isEqualTo(seat.seatNumber)
        Assertions.assertThat(savedOne.price).isEqualByComparingTo(seat.price)
        Assertions.assertThat(savedOne.isAvailable).isEqualTo(seat.isAvailable)
    }

    @Test
    fun `should return found seat that has the id`() {
        // given
        val seat = createSeat(1L, false)

        val savedOne = seatRepository.save(seat)

        // when
        val foundSeats = seatRepository.findAllByIds(listOf(savedOne.id))

        // then
        Assertions.assertThat(foundSeats.count()).isEqualTo(1)
        Assertions.assertThat(foundSeats[0].id).isEqualTo(savedOne.id)
        Assertions.assertThat(foundSeats[0].concertEventId).isEqualTo(seat.concertEventId)
        Assertions.assertThat(foundSeats[0].seatNumber).isEqualTo(seat.seatNumber)
        Assertions.assertThat(foundSeats[0].price).isEqualByComparingTo(seat.price)
        Assertions.assertThat(foundSeats[0].isAvailable).isEqualTo(seat.isAvailable)
    }

    @Test
    fun `should return updated seats that has changed isAvailable`() {
        // given
        val concertEventId = 21L
        val seats =
            listOf(
                createSeat(concertEventId, true, "1번"),
                createSeat(concertEventId, true, "2번"),
            )

        // when
        val savedSeats = seatRepository.saveAll(seats)

        // then
        Assertions.assertThat(savedSeats[0].id).isNotEqualTo(seats[0].id)
        Assertions.assertThat(savedSeats[0].isAvailable).isEqualTo(seats[0].isAvailable)
        Assertions.assertThat(savedSeats[1].id).isNotEqualTo(seats[1].id)
        Assertions.assertThat(savedSeats[1].isAvailable).isEqualTo(seats[1].isAvailable)

        // when
        val updatedSeats = seatRepository.saveAll(savedSeats.map { it.makeUnavailable() })

        // then
        Assertions.assertThat(updatedSeats[0].id).isEqualTo(savedSeats[0].id)
        Assertions.assertThat(updatedSeats[0].isAvailable).isEqualTo(false)
        Assertions.assertThat(updatedSeats[1].id).isEqualTo(savedSeats[1].id)
        Assertions.assertThat(updatedSeats[1].isAvailable).isEqualTo(false)
    }

    @Nested
    inner class QueryTest {
        private var concertEventId1: Long = 21L
        private var concertEventId2: Long = 9L

        private lateinit var firstAvailableSeats: List<Seat>
        private lateinit var firstUnavailableSeats: List<Seat>
        private lateinit var secondAvailableSeats: List<Seat>
        private lateinit var secondUnavailableSeats: List<Seat>

        @BeforeEach
        fun beforeEach() {
            firstAvailableSeats =
                seatRepository.saveAll(
                    listOf(
                        createSeat(concertEventId1, true, "11번"),
                        createSeat(concertEventId1, true, "12번"),
                        createSeat(concertEventId1, true, "24번"),
                    ),
                )
            firstUnavailableSeats =
                seatRepository.saveAll(
                    listOf(
                        createSeat(concertEventId1, false, "22번"),
                    ),
                )

            secondAvailableSeats = seatRepository.saveAll(listOf(createSeat(concertEventId2, true, "1번")))
            secondUnavailableSeats = seatRepository.saveAll(listOf(createSeat(concertEventId2, false, "2번")))
        }

        @Test
        fun `should return seats that has concertEventId1 and is available`() {
            // when
            val foundSeats = seatRepository.findAllAvailableByConcertEventId(concertEventId1)

            // then
            Assertions.assertThat(foundSeats.count()).isEqualTo(firstAvailableSeats.count())
            foundSeats.forEachIndexed { idx, foundOne ->
                Assertions.assertThat(foundOne.id).isEqualTo(firstAvailableSeats[idx].id)
                Assertions.assertThat(foundOne.concertEventId).isEqualTo(concertEventId1)
                Assertions.assertThat(foundOne.isAvailable).isEqualTo(true)
            }
        }

        @Test
        fun `should return all seats that has concertEventId2`() {
            // given
            val concertEvent2Seats = secondAvailableSeats + secondUnavailableSeats
            val concertEvent2SeatIds = concertEvent2Seats.map { it.id }

            // when
            val foundSeats = seatRepository.findAllByIds(concertEvent2SeatIds)

            // then
            Assertions.assertThat(foundSeats.count()).isEqualTo(concertEvent2SeatIds.count())
            foundSeats.forEachIndexed { idx, foundOne ->
                Assertions.assertThat(foundOne.concertEventId).isEqualTo(concertEventId2)
                Assertions.assertThat(foundOne.isAvailable).isEqualTo(concertEvent2Seats[idx].isAvailable)
            }
        }

        @Test
        fun `should return only available seats`() {
            // given
            val concertEvent1Ids = firstAvailableSeats.map { it.id } + firstUnavailableSeats.map { it.id }

            // when
            val foundSeats = seatRepository.findAllAvailableByIds(concertEvent1Ids)

            // then
            Assertions.assertThat(foundSeats.count()).isEqualTo(firstAvailableSeats.count())
            foundSeats.forEach {
                Assertions.assertThat(it.concertEventId).isEqualTo(concertEventId1)
                Assertions.assertThat(it.isAvailable).isEqualTo(true)
            }
        }
    }

    private fun createSeat(
        concertEventId: Long,
        isAvailable: Boolean,
        seatNumber: String = "${concertEventId}번",
    ): Seat =
        Seat(
            id = 0L,
            concertEventId = concertEventId,
            seatNumber = seatNumber,
            price = BigDecimal(450000),
            isAvailable = isAvailable,
        )
}
