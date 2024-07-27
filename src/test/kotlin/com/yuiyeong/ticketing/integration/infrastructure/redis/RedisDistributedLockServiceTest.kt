package com.yuiyeong.ticketing.integration.infrastructure.redis

import com.yuiyeong.ticketing.infrastructure.redis.RedisDistributedLockService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@Testcontainers
class RedisDistributedLockServiceTest {
    @Autowired
    private lateinit var distributedLockService: RedisDistributedLockService

    @Nested
    inner class WithLockTest {
        @Test
        fun `should execute action when lock is acquired`() {
            // when
            val result = distributedLockService.withLock("withLock:testKey") { "success" }

            // then
            Assertions.assertThat(result).isEqualTo("success")
        }

        @Test
        fun `should not execute action when lock is already held`() {
            // given
            val key = "withLock:testKey-for-already-held"
            val executorService = Executors.newFixedThreadPool(2)
            executorService.submit {
                distributedLockService.withLock(key) {
                    Thread.sleep(500)
                    "first"
                }
            }

            // when
            val result = executorService.submit<String?> { distributedLockService.withLock(key) { "second" } }.get()

            // then
            Assertions.assertThat(result).isNull()
            executorService.shutdown()
        }
    }

    @Nested
    inner class WithLockAndRetryTest {
        @Test
        fun `should execute action when lock is acquired`() {
            // when
            val result = distributedLockService.withLockAndRetry("withLockAndRetry:testKey") { "success" }

            // then
            Assertions.assertThat(result).isEqualTo("success")
        }

        @Test
        fun `should retry and succeed if lock is acquired later`() {
            // given
            val key = "withLockAndRetry:testKey-for-retry"
            val executorService = Executors.newFixedThreadPool(2)
            val latch = CountDownLatch(1)

            // given: lock 획득 후 1초 동안 작업 진행하는 thread
            executorService.submit {
                distributedLockService.withLock(key) {
                    Thread.sleep(500)
                    latch.countDown()
                    Thread.sleep(500)
                    "first"
                }
            }
            latch.await()

            // when: 기다려서 lock 획득 후 작업 진행
            val result = executorService.submit<String?> { distributedLockService.withLockAndRetry(key) { "second" } }.get()

            // then
            Assertions.assertThat(result).isEqualTo("second")

            executorService.shutdown()
        }

        @Test
        fun `should return null if lock is never acquired`() {
            // given
            val key = "withLockAndRetry:testKey-for-never-acquired"
            val executorService = Executors.newFixedThreadPool(2)
            val latch = CountDownLatch(1)

            // given: 락 획득 후 6 초 동안 작업 진행
            executorService.submit {
                distributedLockService.withLock(key) {
                    Thread.sleep(1000)
                    latch.countDown()
                    Thread.sleep(5000)
                    "first"
                }
            }
            latch.await()

            // when: 락 획득을 기다리지만 결국 못 얻음
            val result = executorService.submit<String?> { distributedLockService.withLockAndRetry(key) { "second" } }.get()

            // then
            Assertions.assertThat(result).isNull()

            executorService.shutdown()
        }
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer<Nothing>("redis:latest").apply {
                withExposedPorts(6379)
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
}
