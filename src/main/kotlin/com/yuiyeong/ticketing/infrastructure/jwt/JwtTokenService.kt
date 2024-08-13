package com.yuiyeong.ticketing.infrastructure.jwt

import com.yuiyeong.ticketing.config.JwtProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.service.queue.TokenService
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenService(
    private val jwtProperties: JwtProperties,
) : TokenService {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
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

    override fun validateToken(token: String): Long {
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
            return claims.payload.subject.toLong()
        } catch (e: JwtException) {
            logger.info("Error occurs when validateToken($token);${e.javaClass.name} | ${e.message}")
            throw InvalidTokenException()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtTokenService::class.java)
    }
}
