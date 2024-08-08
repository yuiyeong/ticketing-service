# 목차

- [1. Indexing 을 진행할 시나리오의 Query 분석](#1-indexing-을-진행할-시나리오의-query-분석)
    - [1.1. 콘서트 조회 시나리오](#11-콘서트-조회-시나리오)
    - [1.2. 한 콘서트의 예약 가능한 공연(이벤트) 조회 시나리오](#12-한-콘서트의-예약-가능한-공연이벤트-조회-시나리오)
    - [1.3. 콘서트 이벤트의 예약 가능한 좌석 조회 시나리오](#13-콘서트-이벤트의-예약-가능한-좌석-조회-시나리오)
    - [1.4. 결제에서 사용자 지갑 조회 시나리오](#14-결제에서-사용자-지갑-조회-시나리오)
- [2. 데이터셋 구성별 indexing 에 따른 성능 비교 및 적용](#2-데이터셋-구성별-indexing-에-따른-성능-비교-및-적용)
    - [2.0. 테스트 환경 및 방법](#20-테스트-환경-및-방법)
    - [2.1. 한 콘서트의 예약 가능한 공연(이벤트) 조회 테스트](#21-한-콘서트의-예약-가능한-공연이벤트-조회-테스트)
        - [2.1.0. EXPLAIN 쿼리](#210-explain-쿼리)
        - [2.1.1. 소규모 데이터셋](#211-소규모-데이터셋)
        - [2.1.2. 중규모 데이터셋](#212-중규모-데이터셋)
        - [2.1.3. 대규모 데이터셋](#213-대규모-데이터셋)
        - [2.1.4. 최종 결론](#214-최종-결론)
        - [2.1.5. Indexing 적용](#215-indexing-적용)
    - [2.2. 특정 콘서트 공연의 예약 가능한 좌석 조회 테스트](#22-특정-콘서트-공연의-예약-가능한-좌석-조회-테스트)
        - [2.2.0. EXPLAIN 쿼리](#220-explain-쿼리)
        - [2.2.1. 소규모 데이터셋](#221-소규모-데이터셋)
        - [2.2.2. 중규모 데이터셋](#222-중규모-데이터셋)
        - [2.2.3. 대규모 데이터셋](#223-대규모-데이터셋)
        - [2.2.4. 최근에 만들어진 ConcertEvent 를 통한 테스트](#224-최근에-만들어진-concertevent-를-통한-테스트)
        - [2.2.5. 최종 결론](#225-최종-결론)
        - [2.2.6. Indexing 적용](#226-indexing-적용)

---

# 1. Indexing 을 진행할 시나리오의 Query 분석

## 1.1. 콘서트 조회 시나리오

- 사용하는 Query

    ```sql
    SELECT c1_0.id,
           c1_0.title,
           c1_0.singer,
           c1_0.description,
           c1_0.created_at,
           c1_0.updated_at
    FROM   concert c1_0
    
    ```

- 단순 SELECT 쿼리로, 전체 콘서트 목록을 조회합니다.
- 대량 트래픽 시 성능 저하 가능성이 있어, 캐싱이 적용되어있습니다.
- 현재 Use Case 및 로직 상, 콘서트에서 발생하는 쿼리는 위 쿼리밖에 없어서 indexing 을 고려하지 않았습니다.
- 향후 콘서트 필터링이 추가될 경우, `title` 또는 `singer` 컬럼에 대한 인덱스 추가를 고려해볼 수 있습니다.

## 1.2. 한 콘서트의 예약 가능한 공연(이벤트) 조회 시나리오

- 사용하는 Query

    ```sql
    SELECT cee1_0.id,
           cee1_0.created_at,
           cee1_0.updated_at,
           cee1_0.available_seat_count,
           cee1_0.concert_id,
           cee1_0.duration,
           cee1_0.max_seat_count,
           cee1_0.reservation_end_at,
           cee1_0.reservation_start_at,
           cee1_0.start_at,
           cee1_0.venue
    FROM   concert_event cee1_0
    WHERE  cee1_0.concert_id = :concertId
           AND cee1_0.reservation_start_at <= :current
           AND :current <= cee1_0.reservation_end_at;
    
    SELECT ce1_0.id,
           ce1_0.created_at,
           ce1_0.updated_at,
           ce1_0.description,
           ce1_0.singer,
           ce1_0.title
    FROM   concert ce1_0
    WHERE  ce1_0.id = :concertId;
    
    ```

- 특정 콘서트의 공연(이벤트)를 조회하는 쿼리입니다.
- 다음과 같은 index 를 추가했을 때, 성능 개선이 어느정도 나타나는지 확인해 볼 필요가 있습니다.
    - 단일 인덱스
        - Case 1. `concert_id` 만 인덱스를 추가한다.
        - Case 2. `concert_id`, `reservation_start_at` 에 각각 인덱스를 추가한다.
        - Case 3. `consert_id`, `reservation_start_at`, `reservation_end_at` 에 각각 인덱스를 추가한다.
    - 복합 인덱스
        - Case 1. `(concert_id, reservation_start_at)` 순으로 복합 인덱스를 추가한다.
        - Case 2. `(concert_id, reservation_start_at, reservation_end_at)` 순으로 복합 인덱스를 추가하며, `reservation_start_at`
          과 `reservation_end_at` 을 오름차순으로 정렬하도록 한다.
        - Case 3. `(concert_id, reservation_start_at, reservation_end_at)` 순으로 복합 인덱스를 추가하며, `reservation_start_at`
          과 `reservation_end_at` 을 내림차순으로 정렬하도록 한다.

## 1.3. 콘서트 이벤트의 예약 가능한 좌석 조회 시나리오

- 사용하는 Query

    ```sql
    SELECT se1_0.id,
           se1_0.concert_event_id,
           se1_0.is_available,
           se1_0.price,
           se1_0.seat_number,
           se1_0.created_at,
           se1_0.updated_at
    FROM   seat se1_0
    WHERE  se1_0.concert_event_id = ?
           AND se1_0.is_available = 1;
    
    ```

- 특정 콘서트 이벤트에서 예약 가능한(`isAvailable` 이 true 인) 좌석을 조회하는 쿼리입니다.
- 다음과 같은 index 를 추가했을 때, 성능 개선이 어느정도 나타나는지 확인해 볼 필요가 있습니다.
    - 단일 인덱스
        - Case 1. `concert_event_id` 에만 인덱스를 추가한다.
        - Case 2. `is_available` 에만 인덱스를 추가한다.
        - Case 3. `concert_event_id` 와 `is_available` 에 각각 인덱스를 추가한다.
    - 복합 인덱스
        - Case 1. `(concert_event_id, is_available)` 순으로 복합 인덱스를 추가한다.
        - Case 2. `(concert_event_id, is_available)` 순으로 복합 인덱스를 추가하며, `concert_event_id` 를 내림차순으로 정렬하도록 한다.
        - Case 3. `(is_available, concert_event_id)` 순으로 복합 인덱스를 추가한다.

## 1.4. 결제에서 사용자 지갑 조회 시나리오

- 사용하는 Query

    ```sql
    SELECT w1_0.id,
           w1_0.user_id,
           w1_0.created_at,
           w1_0.updated_at
    FROM   wallet w1_0
    WHERE  w1_0.user_id = ?;
    
    ```

- 결제를 진행할 때, 특정 사용자의 지갑을 가져오는 쿼리입니다.
- `wallet`테이블의 경우,`user_id`에 unique key 제약이 있습니다.
- 즉, 이미 indexing 이 되어있으므로 추가 indexing 은 필요 없을 것으로 보입니다.

# 2. 데이터셋 구성별 indexing 에 따른 성능 비교 및 적용

## 2.0. 테스트 환경 및 방법

- Docker 를 이용하여 환경을 구성하였습니다.
- DBMS: MySQL 8.0
- CPU: 2 core
- Memory: 4GB
- 테스트 방법
    1. 데이터셋을 구성한 뒤, Case 에 맞는 Indexing 을 합니다.
    2. `Concert` 혹은 `ConcertEvent` 는 각 전체 목록에서 랜덤으로 10개를 선정합니다.
    3. `SELECT` 에 대한 `EXPLAIN ANALYSIS` 를 총 10번 반복합니다.
    4. 10번의 결과에 대한 평균치를 계산하여 사용합니다.

## 2.1. 한 콘서트의 예약 가능한 공연(이벤트) 조회 테스트

### 2.1.0. EXPLAIN 쿼리

```sql
EXPLAIN ANALYZE
SELECT cee1_0.id,
       cee1_0.created_at,
       cee1_0.updated_at,
       cee1_0.available_seat_count,
       cee1_0.concert_id,
       cee1_0.duration,
       cee1_0.max_seat_count,
       cee1_0.reservation_end_at,
       cee1_0.reservation_start_at,
       cee1_0.start_at,
       cee1_0.venue
FROM concert_event cee1_0
WHERE cee1_0.concert_id = ?
  AND cee1_0.reservation_start_at <= ?
  AND ? < cee1_0.reservation_end_at;

```

### 2.1.1. 소규모 데이터셋

- 100개의 콘서트, 각 콘서트당 20개의 콘서트 이벤트(총 2,000개의 콘서트 이벤트)

**현재 시간이 예약 기간 내인 이벤트 90% 와 현재 시간이 예약 기간 외인 이벤트 10%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용 | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|-------|--------|--------|
| No INDEX                                                            | 0.295         | 204.0 | 22.2   | 22     |
| Case 1. concert_id                                                  | 0.027         | 5.22  | 2.22   | 2      |
| Case 2. concert_id, reservation_start_at                            | 0.020         | 5.67  | 6.67   | 6      |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.019         | 6.8   | 18.0   | 18     |
| Case 4. (concert_id, reservation_start_at)                          | 0.010         | 9.26  | 6.67   | 6      |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.006         | 9.26  | 20.0   | 20     |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.007         | 9.26  | 20.0   | 20     |

**현재 시간이 예약 기간 내인 이벤트 50% 와 현재 시간이 예약 기간 외인 이벤트 50%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용 | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|-------|--------|--------|
| No INDEX                                                            | 0.223         | 204.0 | 22.2   | 22     |
| Case 1. concert_id                                                  | 0.021         | 5.22  | 2.22   | 2      |
| Case 2. concert_id, reservation_start_at                            | 0.022         | 5.67  | 6.67   | 6      |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.019         | 6.0   | 10.0   | 10     |
| Case 4. (concert_id, reservation_start_at)                          | 0.019         | 9.26  | 6.67   | 6      |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.007         | 9.26  | 20.0   | 20     |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.005         | 9.26  | 20.0   | 20     |

**현재 시간이 예약 기간 내인 이벤트 10% 와 현재 시간이 예약 기간 외인 이벤트 90%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용 | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|-------|--------|--------|
| No INDEX                                                            | 0.283         | 204.0 | 22.2   | 22     |
| Case 1. concert_id                                                  | 0.041         | 5.22  | 2.22   | 2      |
| Case 2. concert_id, reservation_start_at                            | 0.019         | 5.67  | 6.67   | 6      |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.019         | 5.20  | 2.0    | 2      |
| Case 4. (concert_id, reservation_start_at)                          | 0.019         | 9.26  | 6.67   | 6      |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.008         | 9.26  | 20.0   | 20     |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.006         | 9.26  | 20.0   | 20     |

**결론**

| 인덱스 구성                                                              | 9:1 일 때, 쿼리 실행 시간 (ms) | 5:5 일 때, 쿼리 실행 시간 (ms) | 1:9 일 때, 쿼리 실행 시간 (ms) |
|---------------------------------------------------------------------|------------------------|------------------------|------------------------|
| No INDEX                                                            | 0.295                  | 0.223                  | 0.283                  |
| Case 1. concert_id                                                  | 0.027                  | 0.021                  | 0.041                  |
| Case 2. concert_id, reservation_start_at                            | 0.020                  | 0.022                  | 0.019                  |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.019                  | 0.019                  | 0.019                  |
| Case 4. (concert_id, reservation_start_at)                          | 0.010                  | 0.019                  | 0.019                  |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.006                  | 0.007                  | 0.008                  |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.007                  | 0.005                  | 0.006                  |

- 인덱스가 없는 경우에 비해 모든 인덱스 구성이 성능을 크게 향상시켰습니다.
- 복합 인덱스(Case 5, 6)가 가장 우수한 성능을 보였습니다.
- 데이터 분포(9:1, 5:5, 1:9)에 따른 성능 차이는 미미했습니다.

### 2.1.2. 중규모 데이터셋

- 5,000개의 콘서트, 각 콘서트당 100개의 콘서트 이벤트(총 500,000개의 콘서트 이벤트)

**현재 시간이 예약 기간 내인 이벤트 90% 와 현재 시간이 예약 기간 외인 이벤트 10%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                                            | 50.831        | 50518.0 | 5519.0 | 5519   |
| Case 1. concert_id                                                  | 0.068         | 26.1    | 11.1   | 11     |
| Case 2. concert_id, reservation_start_at                            | 0.068         | 26.7    | 16.7   | 16     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.066         | 27.5    | 25.0   | 25     |
| Case 4. (concert_id, reservation_start_at)                          | 0.031         | 45.3    | 33.3   | 33     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.008         | 45.3    | 100.0  | 100    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.007         | 45.3    | 100.0  | 100    |

**현재 시간이 예약 기간 내인 이벤트 50% 와 현재 시간이 예약 기간 외인 이벤트 50%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                                            | 68.910        | 50518.0 | 5519.0 | 5519   |
| Case 1. concert_id                                                  | 0.093         | 26.1    | 11.1   | 11     |
| Case 2. concert_id, reservation_start_at                            | 0.066         | 26.7    | 16.7   | 16     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.064         | 27.5    | 25.0   | 25     |
| Case 4. (concert_id, reservation_start_at)                          | 0.058         | 45.3    | 33.3   | 33     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.012         | 45.3    | 100.0  | 100    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.006         | 45.3    | 100.0  | 100    |

**현재 시간이 예약 기간 내인 이벤트 10% 와 현재 시간이 예약 기간 외인 이벤트 90%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|---------------------------------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                                            | 47.030        | 50518.0 | 5519.0 | 5519   |
| Case 1. concert_id                                                  | 0.105         | 26.1    | 11.1   | 11     |
| Case 2. concert_id, reservation_start_at                            | 0.070         | 26.7    | 16.7   | 16     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.072         | 26.0    | 10.1   | 10     |
| Case 4. (concert_id, reservation_start_at)                          | 0.079         | 45.3    | 33.3   | 33     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.020         | 45.3    | 100.0  | 100    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.011         | 45.3    | 100.0  | 100    |

**결론**

| 인덱스 구성                                                              | 9:1 일 때, 쿼리 실행 시간 (ms) | 5:5 일 때, 쿼리 실행 시간 (ms) | 1:9 일 때, 쿼리 실행 시간 (ms) |
|---------------------------------------------------------------------|------------------------|------------------------|------------------------|
| No INDEX                                                            | 50.831                 | 68.910                 | 47.030                 |
| Case 1. concert_id                                                  | 0.068                  | 0.093                  | 0.105                  |
| Case 2. concert_id, reservation_start_at                            | 0.068                  | 0.066                  | 0.070                  |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.066                  | 0.064                  | 0.072                  |
| Case 4. (concert_id, reservation_start_at)                          | 0.031                  | 0.058                  | 0.079                  |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.008                  | 0.012                  | 0.020                  |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.007                  | 0.006                  | 0.011                  |

- 인덱스가 없는 경우와 인덱스가 있는 경우의 성능 차이가 더욱 두드러졌습니다.
- 복합 인덱스(Case 5, 6)가 여전히 가장 우수한 성능을 보였습니다.
- 데이터 분포에 따른 성능 차이가 약간 있지만, 유의미한 수준은 아닙니다.

### 2.1.3. 대규모 데이터셋

- 10,000개의 콘서트, 각 콘서트 당 200개의 콘서트 이벤트(총 2,000,000 개의 콘서트 이벤트)

**현재 시간이 예약 기간 내인 이벤트 90% 와 현재 시간이 예약 기간 외인 이벤트 10%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수  | 실제 행 수 |
|---------------------------------------------------------------------|---------------|----------|---------|--------|
| No INDEX                                                            | 378.8         | 207871.4 | 22084.0 | 22084  |
| Case 1. concert_id                                                  | 0.09696       | 183.0    | 22.2    | 22     |
| Case 2. concert_id, reservation_start_at                            | 0.07828       | 166.0    | 33.3    | 33     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.08126       | 171.0    | 50.0    | 50     |
| Case 4. (concert_id, reservation_start_at)                          | 0.05649       | 212.0    | 66.7    | 66     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.01806       | 220.0    | 200.0   | 200    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.01569       | 221.0    | 200.0   | 200    |

**현재 시간이 예약 기간 내인 이벤트 50% 와 현재 시간이 예약 기간 외인 이벤트 50%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수  | 실제 행 수 |
|---------------------------------------------------------------------|---------------|----------|---------|--------|
| No INDEX                                                            | 367.7         | 208853.8 | 22071.0 | 22071  |
| Case 1. concert_id                                                  | 0.10064       | 181.9    | 22.2    | 22     |
| Case 2. concert_id, reservation_start_at                            | 0.07582       | 167.0    | 33.3    | 33     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.07777       | 169.0    | 50.0    | 50     |
| Case 4. (concert_id, reservation_start_at)                          | 0.12087       | 211.0    | 66.7    | 66     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.03122       | 224.0    | 200.0   | 200    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.02564       | 224.0    | 200.0   | 200    |

**현재 시간이 예약 기간 내인 이벤트 10% 와 현재 시간이 예약 기간 외인 이벤트 90%**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                                              | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수  | 실제 행 수 |
|---------------------------------------------------------------------|---------------|----------|---------|--------|
| No INDEX                                                            | 314.2         | 208707.4 | 22073.0 | 22073  |
| Case 1. concert_id                                                  | 0.10217       | 182.0    | 22.2    | 22     |
| Case 2. concert_id, reservation_start_at                            | 0.07837       | 166.4    | 33.3    | 33     |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.08862       | 165.7    | 19.9    | 19     |
| Case 4. (concert_id, reservation_start_at)                          | 0.15400       | 211.9    | 66.7    | 66     |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.03776       | 219.0    | 200.0   | 200    |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.01378       | 222.0    | 200.0   | 200    |

**결론**

| 인덱스 구성                                                              | 9:1 일 때, 쿼리 실행 시간(ms) | 5:5 일 때, 쿼리 실행 시간(ms) | 1:9 일 때, 쿼리 실행 시간(ms) |
|---------------------------------------------------------------------|-----------------------|-----------------------|-----------------------|
| No INDEX                                                            | 378.8                 | 367.7                 | 314.2                 |
| Case 1. concert_id                                                  | 0.09696               | 0.10064               | 0.10217               |
| Case 2. concert_id, reservation_start_at                            | 0.07828               | 0.07582               | 0.07837               |
| Case 3. concert_id, reservation_start_at, reservation_end_at        | 0.08126               | 0.07777               | 0.08862               |
| Case 4. (concert_id, reservation_start_at)                          | 0.05649               | 0.12087               | 0.15400               |
| Case 5. (concert_id, reservation_start_at, reservation_end_at) ASC  | 0.01806               | 0.03122               | 0.03776               |
| Case 6. (concert_id, reservation_start_at, reservation_end_at) DESC | 0.01569               | 0.02564               | 0.01378               |

- 인덱스의 중요성이 더욱 명확해졌습니다. 인덱스가 없는 경우 쿼리 실행 시간이 크게 증가했습니다.
- 복합 인덱스(Case 5, 6)가 가장 우수한 성능을 유지했습니다.
- 데이터 분포에 따른 성능 차이가 더 뚜렷해졌습니다. 특히 Case 4에서 이 차이가 두드러집니다.

### 2.1.4. 최종 결론

- 모든 데이터셋에서 인덱스 사용이 쿼리 성능을 크게 향상시켰습니다. 데이터셋 크기가 증가할수록 이 효과는 더욱 두드러집니다.
- `(concert_id, reservation_start_at, reservation_end_at)`의 복합 인덱스(Case 5, 6)가 모든 데이터셋과 데이터 분포에서 가장 우수한 성능을 보였습니다.
- 데이터셋 크기가 증가할수록 데이터 분포가 성능에 미치는 영향이 커졌지만, 복합 인덱스를 사용할 경우 이 영향을 최소화할 수 있었습니다.
- 즉, 복합 인덱스의 성능은 데이터셋 크기가 증가해도 비교적 안정적으로 유지되었습니다. 이는 대규모 시스템에서도 충분히 효과적일 것으로 예상됩니다.
- DESC 순서의 복합 인덱스(Case 6)가 ASC 순서(Case 5)보다 약간 더 나은 성능을 보였습니다. 현재 쿼리에서`reservation_start_at <= ?`
  와`? < reservation_end_at`조건을 사용하고 있습니다. DESC 인덱스는 가장 최근의 날짜부터 시작하므로, 현재 시간과 가까운 예약 기간을 먼저 찾을 수 있습니다.
- 일반적으로 콘서트 예약 시스템에서는 가장 최근의 이벤트가 더 관심을 더 받을 것이며, 시간이 지날 수록 과거의 데이터가 더 많아질 것입니다. DESC 인덱스는 최신 데이터부터 접근하므로, 사용자들이 주로 찾는
  최근 이벤트를 더 빨리 찾을 수 있습니다.

### 2.1.5. Indexing 적용

- `한 콘서트의 예약 가능한 공연(이벤트) 조회`의 쿼리에서 사용하는 concert_event
  테이블에,`(concert_id, reservation_start_at DESC, reservation_end_at DESC)`인덱스를 추가하였습니다.

```kotlin
@Entity
@Table(
    name = "concert_event",
    indexes = [
        Index(
            name = "idx_concert_reservation_date_desc",
            columnList = "concert_id, reservation_start_at DESC, reservation_end_at DESC",
        ),
    ],
)
class ConcertEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val concert: ConcertEntity,
    val venue: String,
    val reservationStartAt: ZonedDateTime,
    val reservationEndAt: ZonedDateTime,
    val startAt: ZonedDateTime,
    val duration: Long,
    val maxSeatCount: Int,
    val availableSeatCount: Int,
    @Embedded
    val auditable: Auditable = Auditable(),
)
```

## 2.2. 특정 콘서트 공연의 예약 가능한 좌석 조회 테스트

### 2.2.0. EXPLAIN 쿼리

```sql
EXPLAIN ANALYZE
SELECT se1_0.id,
       se1_0.concert_event_id,
       se1_0.is_available,
       se1_0.price,
       se1_0.seat_number,
       se1_0.created_at,
       se1_0.updated_at
FROM seat se1_0
WHERE se1_0.concert_event_id = ?
  AND se1_0.is_available = 1;

```

### 2.2.1. 소규모 데이터셋

- 1,000개의 콘서트 이벤트, 각 콘서트 이벤트 당 50개의 좌석(총 50,000개의 좌석)

**대부분의 좌석이 예약 가능한 경우 (`is_available`= true 가 90% 이상)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용  | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|--------|--------|--------|
| No INDEX                                      | 4.958         | 4848.0 | 48480  | 2424   |
| Case 1. concert_event_id                      | 0.06234       | 3.25   | 50     | 25     |
| Case 2. is_available                          | 11.797        | 243.0  | 24240  | 2424   |
| Case 3. concert_event_id, is_available        | 0.03372       | 3.25   | 50     | 25     |
| Case 4. (concert_event_id, is_available)      | 0.02749       | 5.25   | 45     | 45     |
| Case 5. (concert_event_id DESC, is_available) | 0.03096       | 5.25   | 45     | 45     |
| Case 6. (is_available, concert_event_id)      | 0.03001       | 5.25   | 45     | 45     |

**절반 정도의 좌석이 예약 가능한 경우 (`is_available`= true 가 약 50%)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용  | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|--------|--------|--------|
| No INDEX                                      | 5.269         | 4856.0 | 48559  | 2428   |
| Case 1. concert_event_id                      | 0.05442       | 3.25   | 50     | 25     |
| Case 2. is_available                          | 7.127         | 244.0  | 24279  | 2428   |
| Case 3. concert_event_id, is_available        | 0.03667       | 3.25   | 50     | 25     |
| Case 4. (concert_event_id, is_available)      | 0.02263       | 3.25   | 25     | 25     |
| Case 5. (concert_event_id DESC, is_available) | 0.01899       | 3.25   | 25     | 25     |
| Case 6. (is_available, concert_event_id)      | 0.01762       | 3.25   | 25     | 25     |

**대부분의 좌석이 예약 불가능한 경우 (`is_available`= true 가 10% 이하)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용  | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|--------|--------|--------|
| No INDEX                                      | 4.14321       | 4834.0 | 48338  | 2417   |
| Case 1. concert_event_id                      | 0.03791       | 3.25   | 50     | 25     |
| Case 2. is_available                          | 1.2014        | 50.8   | 5000   | 500    |
| Case 3. concert_event_id, is_available        | 0.03178       | 1.27   | 50     | 5      |
| Case 4. (concert_event_id, is_available)      | 0.007381      | 1.25   | 5      | 5      |
| Case 5. (concert_event_id DESC, is_available) | 0.007392      | 1.25   | 5      | 5      |
| Case 6. (is_available, concert_event_id)      | 0.009782      | 1.25   | 5      | 5      |

**결론**

| 인덱스 구성                                        | 예약 가능이 90% | 예약 가능이 50% | 예약 가능이 10%  |
|-----------------------------------------------|------------|------------|-------------|
| No INDEX                                      | 4.958 ms   | 5.269 ms   | 4.14321 ms  |
| Case 1. concert_event_id                      | 0.06234 ms | 0.05442 ms | 0.03791 ms  |
| Case 2. is_available                          | 11.797 ms  | 7.127 ms   | 1.2014 ms   |
| Case 3. concert_event_id, is_available        | 0.03372 ms | 0.03667 ms | 0.03178 ms  |
| Case 4. (concert_event_id, is_available)      | 0.02749 ms | 0.02263 ms | 0.007381 ms |
| Case 5. (concert_event_id DESC, is_available) | 0.03096 ms | 0.01899 ms | 0.007392 ms |
| Case 6. (is_available, concert_event_id)      | 0.03001 ms | 0.01762 ms | 0.009782 ms |

- Case 2를 제외한, 모든 상황에서 인덱스가 없는 경우(No INDEX)가 가장 느린 성능을 보입니다.
- `concert_event_id`에만 인덱스를 걸었을 때(Case 1)도 상당한 성능 향상을 보였습니다.
- `is_available`에만 인덱스를 걸었을 때(Case 2)는 예약 가능한 좌석이 적을수록 성능이 좋아지지만, 카디널리티가 더 높은 `concert_event_id` 에 인덱스를 걸었을 때 보다 현저히 낮은
  성능 수준을 보입니다.
- `concert_event_id`와 `is_available`에 각각 인덱스를 걸었을 때(Case 3)는 Case 1보다 약간 더 나은 성능을 보입니다.
- 복합 인덱스를 사용한 Case 4, 5, 6이 전반적으로 가장 좋은 성능을 보여줍니다.
- 예약 가능한 좌석의 비율이 낮아질수록 전반적인 쿼리 실행 시간이 줄어드는 경향이 있습니다. 이는 필터링되는 데이터의 양이 줄어들기 때문으로 보입니다.

### 2.2.2. 중규모 데이터셋

- 10,000개의 콘서트 이벤트, 각 콘서트 이벤트 당 100개의 좌석(총 1,000,000개의 좌석)

**대부분의 좌석이 예약 가능한 경우 (`is_available`= true 가 90% 이상)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                      | 130.08        | 94254.0 | 942536 | 47127  |
| Case 1. concert_event_id                      | 0.06899       | 5.75    | 100    | 50     |
| Case 2. is_available                          | 657.86        | 12416.9 | 527277 | 52727  |
| Case 3. concert_event_id, is_available        | 0.22621       | 58.0    | 100    | 50     |
| Case 4. (concert_event_id, is_available)      | 0.12455       | 55.91   | 90     | 90     |
| Case 5. (concert_event_id DESC, is_available) | 0.1376        | 56.3    | 90     | 90     |
| Case 6. (is_available, concert_event_id)      | 0.21370       | 56.33   | 90     | 90     |

**절반 정도의 좌석이 예약 가능한 경우 (`is_available`= true 가 약 50%)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                      | 122.709       | 93695.0 | 936948 | 46847  |
| Case 1. concert_event_id                      | 0.06156       | 5.75    | 100    | 50     |
| Case 2. is_available                          | 327.73        | 10728.4 | 483468 | 48347  |
| Case 3. concert_event_id, is_available        | 0.21729       | 57.58   | 100    | 50     |
| Case 4. (concert_event_id, is_available)      | 0.13432       | 31.0    | 50     | 50     |
| Case 5. (concert_event_id DESC, is_available) | 0.13816       | 31.2    | 50     | 50     |
| Case 6. (is_available, concert_event_id)      | 0.12837       | 31.12   | 50     | 50     |

**대부분의 좌석이 예약 불가능한 경우 (`is_available`= true 가 10% 이하)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용   | 예상 행 수 | 실제 행 수 |
|-----------------------------------------------|---------------|---------|--------|--------|
| No INDEX                                      | 103.84        | 94152.0 | 941515 | 47076  |
| Case 1. concert_event_id                      | 0.06392       | 5.75    | 100    | 50     |
| Case 2. is_available                          | 168.862       | 3841.3  | 202734 | 20273  |
| Case 3. concert_event_id, is_available        | 0.19088       | 54.2    | 100    | 21     |
| Case 4. (concert_event_id, is_available)      | 0.07669       | 6.071   | 10     | 10     |
| Case 5. (concert_event_id DESC, is_available) | 0.06898       | 6.108   | 10     | 10     |
| Case 6. (is_available, concert_event_id)      | 0.02969       | 6.123   | 10     | 10     |

**결론**

| 인덱스 구성                                        | 예약 가능이 90% | 예약 가능이 50% | 예약 가능이 10% |
|-----------------------------------------------|------------|------------|------------|
| No INDEX                                      | 130.08 ms  | 122.709 ms | 103.84 ms  |
| Case 1. concert_event_id                      | 0.06899 ms | 0.06156 ms | 0.06392 ms |
| Case 2. is_available                          | 657.86 ms  | 327.73 ms  | 168.862 ms |
| Case 3. concert_event_id, is_available        | 0.22621 ms | 0.21729 ms | 0.19088 ms |
| Case 4. (concert_event_id, is_available)      | 0.12455 ms | 0.13432 ms | 0.07669 ms |
| Case 5. (concert_event_id DESC, is_available) | 0.1376 ms  | 0.13816 ms | 0.06898 ms |
| Case 6. (is_available, concert_event_id)      | 0.21370 ms | 0.12837 ms | 0.02969 ms |

- `concert_event_id`에만 인덱스를 걸었을 때(Case 1)도 상당한 성능 향상을 보이지만, 예약 가능 비율에 따른 성능 차이가 크지 않습니다.
- `is_available`에만 인덱스를 걸었을 때(Case 2)는 오히려 성능이 떨어졌습니다.
- `concert_event_id`와 `is_available`에 각각 인덱스를 걸었을 때(Case 3)는 Case 1보다 성능이 떨어집니다.
- 복합 인덱스를 사용한 Case 4, 5, 6 이 전반적으로 좋은 성능을 보여줍니다.
- 예약 가능한 좌석의 비율이 낮아질수록 전반적인 쿼리 실행 시간이 줄어드는 경향이 있습니다.
- `(is_available, concert_event_id)` 순서의 복합 인덱스(Case 6)는 예약 가능한 좌석의 비율이 낮을 때 특히 좋은 성능을 보이는데, 이는 `is_available` 을 통해 필터링된
  데이터 양이 적어서 생기는 현상같습니다.

### 2.2.3. 대규모 데이터셋

- 20,000개의 콘서트 이벤트, 각 콘서트 이벤트 당 500개의 좌석(총 10,000,000 개의 좌석)

**대부분의 좌석이 예약 가능한 경우 (`is_available`= true 가 90% 이상)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수   | 실제 행 수 |
|-----------------------------------------------|---------------|----------|----------|--------|
| No INDEX                                      | 1064.14       | 989559.3 | 463636.0 | 463636 |
| Case 1. concert_event_id                      | 0.0823        | 525.0    | 250.0    | 250    |
| Case 2. is_available                          | 7074.0        | 241646.0 | 463636.0 | 463636 |
| Case 3. concert_event_id, is_available        | 0.09029       | 525.0    | 250.0    | 250    |
| Case 4. (concert_event_id, is_available)      | 0.09333       | 495.0    | 450.0    | 450    |
| Case 5. (concert_event_id DESC, is_available) | 0.08779       | 495.0    | 450.0    | 450    |
| Case 6. (is_available, concert_event_id)      | 0.089         | 495.0    | 450.0    | 450    |

**절반 정도의 좌석이 예약 가능한 경우 (`is_available`= true 가 약 50%)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수   | 실제 행 수 |
|-----------------------------------------------|---------------|----------|----------|--------|
| No INDEX                                      | 1157.6        | 1.13     | 535666.0 | 535666 |
| Case 1. concert_event_id                      | 0.10293       | 525.0    | 250.0    | 250    |
| Case 2. is_available                          | 4832.28       | 242334.2 | 535666.0 | 535666 |
| Case 3. concert_event_id, is_available        | 0.07851       | 525.0    | 250.0    | 250    |
| Case 4. (concert_event_id, is_available)      | 0.10182       | 275.0    | 250.0    | 250    |
| Case 5. (concert_event_id DESC, is_available) | 0.09726       | 275.0    | 250.0    | 250    |
| Case 6. (is_available, concert_event_id)      | 0.0855        | 275.0    | 250.0    | 250    |

**대부분의 좌석이 예약 불가능한 경우 (`is_available`= true 가 10% 이하)**

- Case 별`EXPLAIN ANALYZE`결과

| 인덱스 구성                                        | 쿼리 실행 시간 (ms) | 예상 비용    | 예상 행 수   | 실제 행 수 |
|-----------------------------------------------|---------------|----------|----------|--------|
| No INDEX                                      | 1041.48       | 962811.1 | 450304.0 | 450304 |
| Case 1. concert_event_id                      | 0.08027       | 525.0    | 250.0    | 250    |
| Case 2. is_available                          | 958.83        | 211936.1 | 196936.0 | 196936 |
| Case 3. concert_event_id, is_available        | 0.08215       | 511.0    | 109.0    | 109    |
| Case 4. (concert_event_id, is_available)      | 0.07251       | 55.0     | 50.0     | 50     |
| Case 5. (concert_event_id DESC, is_available) | 0.06778       | 55.0     | 50.0     | 50     |
| Case 6. (is_available, concert_event_id)      | 0.06073       | 55.0     | 50.0     | 50     |

**결론**

| 인덱스 구성                                        | 예약 가능이 90% | 예약 가능이 50% | 예약 가능이 10% |
|-----------------------------------------------|------------|------------|------------|
| No INDEX                                      | 1064.14 ms | 1157.6 ms  | 1041.48 ms |
| Case 1. concert_event_id                      | 0.0823 ms  | 0.10293 ms | 0.08027 ms |
| Case 2. is_available                          | 7074.0 ms  | 4832.28 ms | 958.83 ms  |
| Case 3. concert_event_id, is_available        | 0.09029 ms | 0.07851 ms | 0.08215 ms |
| Case 4. (concert_event_id, is_available)      | 0.09333 ms | 0.10182 ms | 0.07251 ms |
| Case 5. (concert_event_id DESC, is_available) | 0.08779 ms | 0.09726 ms | 0.06778 ms |
| Case 6. (is_available, concert_event_id)      | 0.089 ms   | 0.0855 ms  | 0.06073 ms |

- 인덱스 없이 쿼리를 실행하는 것은 매우 비효율적입니다. 모든 경우에서 1초 이상의 실행 시간이 소요되었습니다.
- `concert_event_id`에 대한 단일 인덱스(Case 1)만으로도 큰 성능 향상을 얻을 수 있습니다. 이는 쿼리가 특정 콘서트 이벤트의 좌석만을 조회하기 때문입니다.
- `is_available`컬럼에 대한 단일 인덱스(Case 2)는 오히려 성능을 저하시킵니다. 이는 해당 컬럼이 카디널리티가 낮아, 데이터가 필터링 되더라도 그 양이 많기 때문입니다.
- 복합 인덱스(Case 4, 5, 6)들은 모두 우수한 성능을 보여주며, 특히`is_available`이 10%인 경우에 더 효과적입니다.
- `concert_event_id`를 내림차순으로 인덱싱(Case 5)하는 것도 큰 영향을 주지 않았습니다.

결론적으로, 이 쿼리에 대해서는`concert_event_id`와`is_available`에 대한 복합 인덱스(Case 4, 5, 6)가 가장 효과적입니다.

### 2.2.4. 최근에 만들어진 ConcertEvent 를 통한 테스트

- 데이터셋: 20,000개의 콘서트 이벤트, 각 콘서트 이벤트 당 500개의 좌석(총 10,000,000 개의 좌석)
- 초기 가설로, 시간이 지날수록 콘서트 이벤트 중 최근에 생성된 데이터에 대한 쿼리가 증가할 것이므로, `Case 5(concert_event_id DESC, is_available 복합인덱스)`의 성능이 가장
  우수할 것으로 예상했습니다.
- 하지만 테스트 결과, 복합 인덱스를 사용하는 Case 간의 유의미한 성능 차이가 없었습니다.
- 이러한 테스트 결과는, 전체 콘서트 이벤트 목록에서 무작위로 콘서트 이벤트를 선정하여 테스트를 진행하였기 때문에 생긴 현상이라고 보고, **최근에 만들어진 10%의 콘서트 이벤트에 대해 무작위로 10개를 선정하여
  그 이벤트로 테스트를 다시 진행**하였고 그 결과는 다음 표와 같습니다.

| 인덱스 구성                                        | 예약 가능이 90%   | 예약 가능이 50%  | 예약 가능이 10%  |
|-----------------------------------------------|--------------|-------------|-------------|
| No INDEX                                      | 2439.400 ms  | 2382.200 ms | 2340.600 ms |
| Case 1. concert_event_id                      | 0.073 ms     | 0.069 ms    | 0.067 ms    |
| Case 2. is_available                          | 15580.500 ms | 8996.200 ms | 2787.000 ms |
| Case 3. concert_event_id, is_available        | 0.093 ms     | 0.065 ms    | 0.069 ms    |
| Case 4. (concert_event_id, is_available)      | 0.065 ms     | 0.083 ms    | 0.058 ms    |
| Case 5. (concert_event_id DESC, is_available) | 0.072 ms     | 0.071 ms    | 0.048 ms    |
| Case 6. (is_available, concert_event_id)      | 0.076 ms     | 0.070 ms    | 0.055 ms    |

- 복합 인덱스(Case 4, 5, 6)들은 모두 우수한 성능을 보여주며, 특히 예약 가능한 좌석의 비율이 낮을수록(10% 이하) 더 효과적입니다.
- Case 5는 예약 가능한 좌석이 10% 이하일 때 가장 좋은 성능을 보입니다.
- 종합적으로, `Case 5 (concert_event_id DESC, is_available)`가 가장 균형 잡힌 성능을 보여줍니다. 특히 시간이 지남에 따라 예약 가능한 좌석의 비율이 낮아질 것을 고려하면, 이
  인덱스가 장기적으로 가장 효과적일 것으로 보이며, 최근 콘서트 이벤트에 대한 쿼리가 더 자주 발생할 것이라는 점에서도 이 인덱스가 유리합니다.

### 2.2.5. 최종 결론

- 복합 인덱스(Case 4, 5, 6)는 모든 시나리오에서 일관되게 좋은 성능을 보여줍니다.
- Case 2. `is_available` 은 인덱싱을 했음에도 카디널리티가 낮다면, 오히려 성능 저하를 일으킴을 보여주는 예시였습니다.
- 콘서트 좌석 예매 서비스의 특성은 다음과 같습니다.
    - 콘서트 이벤트 신청 기한 시작 시 대규모 트래픽 발생
    - 짧은 시간 내에 대부분의 좌석이 예약될 것으로 예상 (`isAvailable`이 빠르게 false로 변경)
    - 시간이 지날수록 `isAvailable`이 false인 데이터가 대다수를 차지
    - 시간이 지날수록 최근에 만들어진 콘서트 이벤트에 대한 쿼리가 늘어남
- **_이러한 특성을 고려했을 때, `Case 5 (concert_event_id DESC, is_available)` 로 Indexing 을 하는 것이 적절하다고 생각합니다._**
  - 초기 대규모 트래픽 처리시,
      - `concert_event_id`로 빠르게 특정 콘서트 이벤트의 좌석을 찾을 수 있습니다.
      - `is_available`이 함께 인덱싱되어 있어 가용 좌석 필터링에도 효율적입니다.
  - 시간 경과에 따른 성능에서도,
      - DESC 옵션으로 인해 최신 콘서트 이벤트가 인덱스 앞부분에 위치하여 성능을 높입니다.
      - 시간이 지나도 자주 조회되는 최근 데이터에 대한 접근이 빠릅니다.
  - 서비스 기간이 길어져 `isAvailable`이 false인 데이터가 많아져도, `concert_event_id`로 먼저 필터링하므로 성능에 끼치는 영향을 최소화할 수 있습니다.

### 2.2.6. Indexing 적용

- `한 콘서트 이벤트의 예약 가능한 좌석 조회`의 쿼리에서 사용하는 seat 테이블에,`(concert_event_id DESC, is_available)`인덱스를 추가하였습니다.

```kotlin
@Entity
@Table(
    name = "seat",
    indexes = [
        Index(
            name = "idx_concert_event_desc_available",
            columnList = "concert_event_id DESC, is_available",
        ),
    ],
)
class SeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val concertEventId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
    @Embedded
    val auditable: Auditable = Auditable(),
)
```