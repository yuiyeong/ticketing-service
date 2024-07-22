package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.reservation.ReservationStatus
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class ReservationRepositoryTest {
    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Test
    fun `should return a reservation that has new id after saving it`() {
        // given
        val userId = 12L
        val reservation = createReservation(userId, ReservationStatus.PENDING)

        // when
        val savedOne = reservationRepository.save(reservation)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(reservation.id)
        Assertions.assertThat(savedOne.userId).isEqualTo(reservation.userId)
        Assertions.assertThat(savedOne.status).isEqualTo(reservation.status)
        Assertions.assertThat(savedOne.createdAt).isNotNull()
    }

    @Test
    fun `should return found reservation that the id`() {
        // given
        val userId = 21L
        val reservation = createReservation(userId, ReservationStatus.CONFIRMED)
        val savedOne = reservationRepository.save(reservation)

        // when
        val foundOne = reservationRepository.findOneById(savedOne.id)

        // then
        Assertions.assertThat(foundOne).isNotNull()
        Assertions.assertThat(foundOne!!.id).isEqualTo(savedOne.id)
        Assertions.assertThat(foundOne.userId).isEqualTo(savedOne.userId)
        Assertions.assertThat(foundOne.concertId).isEqualTo(savedOne.concertId)
        Assertions.assertThat(foundOne.concertEventId).isEqualTo(savedOne.concertEventId)
        Assertions.assertThat(foundOne.status).isEqualTo(savedOne.status)
        Assertions.assertThat(foundOne.totalSeats).isEqualTo(savedOne.totalSeats)
        Assertions.assertThat(foundOne.totalAmount).isEqualByComparingTo(savedOne.totalAmount)
        Assertions.assertThat(foundOne.createdAt).isEqualTo(savedOne.createdAt)
    }

    private fun createReservation(
        userId: Long,
        status: ReservationStatus,
    ): Reservation =
        Reservation(
            id = 0L,
            userId = userId,
            concertId = 2L,
            concertEventId = 3L,
            status = status,
            totalSeats = 1,
            totalAmount = BigDecimal(20000),
            createdAt = ZonedDateTime.now().asUtc,
        )
}
