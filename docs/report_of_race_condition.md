# 동시성 문제

- 이 서비스에서 발생할 수 있는 동시성 문제는 좌석 점유 상황과 지갑 잔액 충전 및 사용 상황에서 발생할 수 있습니다.
- 좌석 점유 상황에서는, 한 좌석에 대한 점유를 여러 명이 동시에 시도했을 때, 1명만 그 좌석을 점유할 수 있어야합니다.
- 지갑 잔액 충전 및 사용 상황에서는, 동시에 사용과 충전을 하더라고 총 잔액은 각각 사용한 금액을 빼고 충전한 금액을 더한 금액과 같아야 합니다.

## 동시성 문제가 발생할 수 있는 로직

- `OccupySeatUseCase::execute` 는 다음의 과정을 수행합니다.
    1. 콘서트 이벤트가 예약 기간인지 확인
    2. seatId 를 가지는 좌석에 대해 점유
    3. 콘서트 이벤트의 선택 가능한 좌석 수 업데이트
    ```kotlin
    fun execute(userId: Long, concertEventId: Long, seatId: Long): OccupationResult {
        // 콘서트 이벤트가 예약 기간인지 확인
        val now = ZonedDateTime.now().asUtc
        val concertEvent = concertService.getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(now)
    
        // seatId 를 가지는 좌석에 대해 점유
        val seatIds = listOf(seatId)
        val occupation = occupationService.occupy(userId, concertEventId, seatIds)
    
        // 콘서트 이벤트의 선택 가능한 좌석 수 업데이트
        concertService.refreshAvailableSeats(concertEventId)
        return OccupationResult.from(occupation)
    }
    ```
- `OccupySeatUseCase::execute` → `OccupationService::occupy` 호출하여 좌석 점유 수행하는데, 이때 동시성 문제가 발생할 수 있습니다.
- 동시성 문제가 발생할 수 있는 `OccupationService::occupy`
    ```kotlin
    @Service
    class OccupationService(
        @Value("\${config.valid-occupied-duration}") private val validOccupiedDuration: Long,
        private val occupationRepository: OccupationRepository,
        private val seatRepository: SeatRepository,
    ) {
        @Transactional
        fun occupy(
            userId: Long,
            concertEventId: Long,
            seatIds: List<Long>,
        ): Occupation {
            if (seatIds.isEmpty()) throw SeatUnavailableException()
            // 좌석을 가져오는 이 부분에서 여러 사람이 같은 좌석에 대해 접근 가능하므로, 동시성 문제가 발생
            val seats = seatRepository.findAllAvailableByIds(seatIds)
    
            if (seats.count() != seatIds.count()) throw SeatUnavailableException()
    
            val occupiedSeats = seatRepository.saveAll(seats.map { it.makeUnavailable() })
            val occupation = Occupation.create(userId, concertEventId, occupiedSeats, validOccupiedDuration)
            return occupationRepository.save(occupation)
        }
    
        // ... 다른 코드 생략 ..
    }
    ```
- WalletService::charge 와 WalletService::pay 에서 지갑을 찾아오는 과정에서 동시성 문제가 발생할 수 있습니다.
    ```kotlin
    class WalletService{
        // ... 다른 코드 생략 ...
        fun createTransaction(
            userId: Long,
            amount: BigDecimal,
            type: TransactionType,
        ): Transaction { 
            // 동시에 지갑의 잔액을 조회할 경우, 동시성 문제가 발생할 수 있다.
            val wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()
            val updatedWallet =
                walletRepository.save(
                    when (type) {
                        TransactionType.CHARGE -> wallet.charge(amount)
                        TransactionType.PAYMENT -> wallet.pay(amount)
                    },
                )
            return transactionRepository.save(Transaction.create(updatedWallet, amount, type))
        }
    }
    ```

## 좌석 점유에 대한 동시성 테스트 코드

```kotlin
@Test
fun `여러 사람이 한 좌석에 대해 점유를 시도해도 1명만 성공해야한다`() {
    // given
    val userIds = (1..10).map { it.toLong() }
    // userId 별 성공 여부
    val results = ConcurrentHashMap<Long, Boolean>()
    val maxWorkingTime = AtomicReference(0L)

    val taskCount = userIds.count()
    val latch = CountDownLatch(taskCount)
    val executor = Executors.newFixedThreadPool(taskCount)

    // when: 10 개의 thread 가 동시에 실행. 10명이 동시에 한 좌석을 점유하려고 함
    userIds.forEach { userId ->
        executor.submit {
            val startTime = System.nanoTime()
            try {
                occupySeatUseCase.execute(userId, concertEvent.id, seat.id)
                results[userId] = true
            } catch (e: Exception) {
                results[userId] = false
            } finally {
                val threadTime = System.nanoTime() - startTime
                maxWorkingTime.updateAndGet { max(it, threadTime) }
                latch.countDown()
            }
        }
    }
    latch.await()
    logger.info("가장 오래 걸린 시간: ${TimeUnit.NANOSECONDS.toMillis(maxWorkingTime.get())} ms")

    // then: 1명만 좌석 점유에 성공
    Assertions.assertThat(results.values.count { it }).isEqualTo(1)
    Assertions.assertThat(results.values.count { !it }).isEqualTo(userIds.count() - 1)

    executor.shutdown()
}
```

## 지갑 잔액 충전 및 사용에 대한 동시성 테스트 코드

```kotlin
@Test
fun `동시에 충전과 사용을 여러번 해도 같은 잔액이 나와야 한다`() {
    // given
    val taskCount = 9 // 3으로 나누어 떨어질 수 있도록
    val latch = CountDownLatch(taskCount)
    val executorService = Executors.newFixedThreadPool(taskCount)

    val chargingAmount = BigDecimal(1000L)
    val payingAmount = BigDecimal(750L)
    val maxWorkingTime = AtomicReference(0L)

    // when: 나머지에 따라, 충전, 사용, 충전 및 사용을 진행한다.
    for (i in 1..taskCount) {
        executorService.submit {
            val startTime = System.nanoTime()
            try {
                when (i % 3) {
                    0 -> walletService.charge(userId, chargingAmount)
                    1 -> walletService.pay(userId, payingAmount)
                    2 -> {
                        walletService.charge(userId, chargingAmount)
                        walletService.pay(userId, payingAmount)
                    }
                }
            } finally {
                val threadTime = System.nanoTime() - startTime
                maxWorkingTime.updateAndGet { max(it, threadTime) }
                latch.countDown()
            }
        }
    }
    latch.await()
    logger.info("가장 오래 걸린 시간: ${TimeUnit.NANOSECONDS.toMillis(maxWorkingTime.get())} ms")

    // then: 최종 잔액은 처음 잔액에서 총 충전 금액을 더하고 총 사용 금액을 뺀 값이다.
    val executedCount = BigDecimal((taskCount / 3) * 2)
    val expectedBalance =
        initialBalance
            .add(chargingAmount.multiply(executedCount))
            .subtract(payingAmount.multiply(executedCount))

    val wallet = walletService.getUserWallet(userId)
    Assertions.assertThat(wallet.balance).isEqualByComparingTo(expectedBalance)

    executorService.shutdown()
}
```

## 좌석 점유에 대한 동시성 테스트 실험

- 테스트에 대한 비교 분석은 좌석 점유에 대해서만 진행하였습니다.

### 테스트 환경

- Macbook Pro M3 MAX 에서 수행했습니다.
- DBMS 는 MySQL 8 을 사용했습니다.
- Connection Pool 은 Hikari 를 사용했습니다.
    - maximum-pool-size: 30
    - connection-timeout: 5000

### 테스트 결과

- 총 10 번 수행하여 실행 시간의 평균을 냈습니다.

#### Case 1. 비관적 락 사용(@Transactional(propagation = Propagation.REQUIRED))

- Connection Pool 의 최대까지 사용하고 있었습니다.

| 사용자 수  | 평균 `OccupySeatUseCase::execute` 실행 시간 |
|--------|---------------------------------------|
| 10명    | 101.4 ms                              |
| 100명   | 153.7 ms                              |
| 1,000명 | 721 ms                                |
| 5,000명 | 4382.3 ms                             |

#### Case 2. 비관적 락 사용(@Transactional(propagation = Propagation.REQUIRES_NEW))

- Connection Pool 의 최대까지 사용하고 있었습니다.

| 테스트 인원 | 평균 실행 시간 (ms) |
|--------|---------------|
| 10명    | 96.4          |
| 100명   | 143.6         |
| 1000명  | 666.2         |
| 5000명  | 3681.6        |

#### Case 3. 낙관적 락 사용(@Transactional(propagation = Propagation.REQUIRES_NEW))

- Connection Pool 의 최대까지 사용하고 있었습니다.

| 사용자 수 | 평균 `OccupySeatUseCase::execute` 실행 시간 |
|-------|---------------------------------------|
| 10명   | 103.8                                 |
| 100명  | 156.8                                 |
| 1000명 | 591.0                                 |
| 5000명 | 3264.0                                |

#### Case 4. 분산락(Simple Lock) + 비관적 락 사용(@Transactional(propagation = Propagation.REQUIRES_NEW))

- Connection 수가 최대 5 개를 넘지 않았습니다.

| 사용자 수 | 평균 `OccupySeatUseCase::execute` 실행 시간 |
|-------|---------------------------------------|
| 10명   | 222.8                                 |
| 100명  | 255.2                                 |
| 1000명 | 409.2                                 |
| 5000명 | 1825.0                                |

## 결론

- `비관적 락`, `낙관적 락`, `분산락 + 비관적 락` 순으로 성능이 향상되는 경향을 보입니다.
    - 좌석 점유에서는 `낙관적 락`의 재시도 로직이 없기 때문에, `비관적 락`과 비슷하거나 나은 수준의 성능을 보입니다. 
- 특히 사용자 수가 증가할수록 `분산락 + 비관적 락` 방식의 성능 우위가 두드러집니다.
- `REQUIRES_NEW` 를 사용한 경우가 `REQUIRED` 를 사용한 경우보다 약간 더 나은 성능을 보입니다.
- `비관적 락`과 `낙관적 락` 방식은 사용자 수 증가에 따라 실행 시간이 급격히 증가하는 반면, `분산락 + 비관적 락` 방식은 상대적으로 완만한 증가를 보입니다.
- `비관적 락`과 `낙관적 락` 방식은 Connection Pool 을 최대로 사용하는 반면, `분산락 + 비관적 락` 방식은 Connection 사용량이 현저히 적어, DB 과부하를 방지할 수 있을 것으로 보입니다.
- `분산락 + 비관적 락` 방식은 적은 리소스 사용과 높은 확장성을 제공하지만, 소규모 사용자에 대해서는 다른 방식보다 실행 시간이 길어지는 측면이 있습니다.
- **_이 서비스는 콘서트 좌석 예매 서비스임을 고려했을 때, `분산락 + 비관적 락` 방식을 사용하는 것이 가장 적합할 것 같습니다._**
    - 콘서트 티켓 예매는 특정 시간에 대량의 동시 접속이 예상되는 서비스입니다. 소수 사용자 상황에서는 약간 느릴 수 있지만, 서비스의 특성상 대규모 동시 접속에 대한 대비가 더 중요합니다. 
    - 실험 결과에서 볼 수 있듯이, `분산락 + 비관적 락` 방식은 사용자 수가 증가해도 실행 시간이 상대적으로 완만하게 증가하므로, 대규모 동시 접속에 대해서 효과적으로 대비를 할 수 있습니다.
    - 또한 Connection Pool 사용량이 현저히 적어, 서버 리소스를 효율적으로 사용할 수 있습니다.