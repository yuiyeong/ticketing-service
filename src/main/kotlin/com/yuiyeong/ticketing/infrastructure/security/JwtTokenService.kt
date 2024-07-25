package com.yuiyeong.ticketing.infrastructure.security

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import com.yuiyeong.ticketing.domain.service.queue.TokenService
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenService(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    private val queueEntryRepository: QueueEntryRepository,
) : TokenService {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    override fun generateToken(
        userId: Long,
        issuedAt: ZonedDateTime,
        expiresAt: ZonedDateTime,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(Date.from(issuedAt.toInstant()))
            .expiration(Date.from(expiresAt.toInstant()))
            .id(UUID.randomUUID().toString())
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()

    @Transactional(readOnly = true)
    override fun validateToken(token: String): QueueEntry {
        try {
            Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
        } catch (e: JwtException) {
            logger.info("Error occurs when validateToken($token);${e.javaClass.name} | ${e.message}")
            throw InvalidTokenException()
        }

        return queueEntryRepository.findOneByToken(token) ?: throw InvalidTokenException()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtTokenService::class.java)
    }
}
