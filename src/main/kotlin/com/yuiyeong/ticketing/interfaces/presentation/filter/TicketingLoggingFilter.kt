package com.yuiyeong.ticketing.interfaces.presentation.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.yuiyeong.ticketing.config.LoggingProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.Locale

@Component
@ConditionalOnProperty(name = ["logging.request-response.enabled"], havingValue = "true")
class TicketingLoggingFilter(
    private val loggingProperties: LoggingProperties,
) : OncePerRequestFilter() {
    private val filterLogger = LoggerFactory.getLogger(TicketingLoggingFilter::class.java)
    private val objectMapper = ObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val responseWrapper = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()

        // request 처리
        filterChain.doFilter(requestWrapper, responseWrapper)

        // 로깅
        logRequestResponse(requestWrapper, responseWrapper, System.currentTimeMillis() - startTime)

        // response 전달
        responseWrapper.copyBodyToResponse()
    }

    private fun logRequestResponse(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        elapsedTime: Long,
    ) {
        val method = request.method
        val uri = request.requestURI
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val status = response.status

        val requestBody = getRequestBody(request)
        val responseBody = getResponseBody(response)

        filterLogger.info("($elapsedTime ms) $method $uri$queryString | $status | $requestBody | $responseBody")
    }

    private fun getRequestBody(request: ContentCachingRequestWrapper): String {
        val contentLength = request.contentLength
        return when {
            contentLength <= 0 -> "Empty body"
            contentLength > loggingProperties.maxContentLength ->
                "Too Large Body($contentLength bytes), summary: ${
                    getBodySummary(
                        request.contentAsByteArray,
                        request.contentType,
                    )
                }"

            else -> getCompactBody(request.contentAsByteArray)
        }
    }

    private fun getResponseBody(response: ContentCachingResponseWrapper): String {
        val contentLength = response.contentSize
        return when {
            contentLength <= 0 -> "Empty body"
            contentLength > loggingProperties.maxContentLength ->
                "Too Large Body($contentLength bytes), summary: ${
                    getBodySummary(
                        response.contentAsByteArray,
                        response.contentType,
                    )
                }"

            else -> getCompactBody(response.contentAsByteArray)
        }
    }

    private fun getBodySummary(
        content: ByteArray,
        contentType: String?,
    ): String {
        val info = mutableListOf<String>()
        info.add("Size: ${content.size} bytes")
        info.add("Type: $contentType")
        info.add("Preview: ${String(content.take(loggingProperties.previewLength).toByteArray())}...")
        return info.joinToString(" | ")
    }

    private fun getCompactBody(content: ByteArray): String {
        if (content.isEmpty()) return "-"

        return try {
            val bodyMap = objectMapper.readValue(content, Map::class.java)
            val compactMap =
                bodyMap.mapValues { (key, value) ->
                    when {
                        // 민감 정보 마스킹
                        key.toString().lowercase(Locale.getDefault()).contains("token") -> "********"
                        value is String && value.length > 100 -> "${value.substring(0, 97)}..."
                        else -> value
                    }
                }
            objectMapper.writeValueAsString(compactMap)
        } catch (e: Exception) {
            // JSON 파싱에 실패한 경우, 문자열의 일부만 반환
            String(content).let { if (it.length > 100) "${it.substring(0, 97)}..." else it }
        }
    }
}
