# API 명세

## _!!WARNING!!_

**이 문서는 더 이상 사용되지 않습니다.**
**다음 [문서](./swagger_api_documents.pdf)에서 API 명세가 업데이트 됩니다.**

## 개요
더 이상 사용되지 않는 문서입니다.
~~이 문서는 콘서트 좌석 예약 서비스의 API 명세를 설명합니다.
사용자에 대한 Authorization 검증은 API Gateway 에서 진행되었다고 가정합니다.~~

## 응답 형식

모든 API 응답은 일관된 형식을 따릅니다. 응답은 성공과 실패의 두 가지 주요 카테고리로 나뉩니다.

### 성공 응답

성공적인 응답은 요청된 데이터의 유형에 따라 두 가지 형식 중 하나를 사용합니다.

#### 단일 객체 응답

단일 객체를 반환할 때, 응답은 `data` 키 아래에 객체를 포함합니다.

**예시**

```json
{
    "data": {
        "id": 1,
        "name": "콘서트 A"
    }
}
```

#### 목록 응답

객체 목록을 반환할 때, 응답은 `list` 키 아래에 객체 배열을 내려줍니다.
**예시**

```json
{
    "list": [
        {
            "id": 1,
            "title": "콘서트 A"
        }
    ]
}
```

#### 오류 응답

오류 발생 시, 응답은 `error` 키 아래에 오류 정보를 포함합니다. 여기에는 오류 코드와 메시지가 포함됩니다.

**예시**

```json
{
    "error": {
        "code": "not_found",
        "message": "요청한 콘서트 정보를 찾을 수 없습니다."
    }
}
```

#### 상태 코드

API 는 표준 HTTP 상태 코드를 사용하여 요청의 결과를 나타냅니다

- `200`: 성공
- `400`: 잘못된 요청
- `404`: 리소스를 찾을 수 없음
- `500`: 서버 내부 오류

#### Error Code

- `invalid_token`
- `not_found_in_queue`
- `not_found_concert`
- `not_found_concert_event`
- `invalid_date`
- `invalid_seat`
- `not_found_seat`
- `invalid_seat_status`
- `insufficient_balance`
- `occupation_expired`
- `invalid_amount`

~~## 대기열 Token 발급~~

사용자를 대기열에 넣고, 그 대기 정보에 대한 token 을 내려줍니다.

### Endpoint

`POST` **/api/v1/queue/token**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |

### Request

#### Params

* 없음

#### Body

| 필드     | 타입     | 필수 | 설명      |
|--------|--------|----|---------|
| userId | number | 예  | 사용자의 id |

**예시**

```json
{
    "userId": 1
}
```

### Response

| 필드                   | 타입     | 설명              |
|----------------------|--------|-----------------|
| token                | string | 대기 정보에 대한 token |
| estimatedWaitingTime | number | 예상 대기 시간(분)     |

**예시**

```json
{
    "data": {
        "token": "a21kTkseTestToken",
        "estimatedWaitingTime": 20
    }
}
```

### Error

* 없음

~~## 대기열 정보 조회~~

발급받은 Token 으로 현재 대기 정보를 조회합니다.

### Endpoint

`GET` **/api/v1/queue/status**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |
| User-Token    | string                | 예  | 대기열 진입 시 발급받은 토큰  |

### Request

#### Params

* 없음

#### Body

* 없음

### Response

| 필드                   | 타입     | 설명           |
|----------------------|--------|--------------|
| queuePosition        | number | 대기열에서의 현재 위치 |
| estimatedWaitingTime | number | 예상 대기 시간(분)  |

**예시**

```json
{
    "data": {
        "queuePosition": 15,
        "estimatedWaitingTime": 10
    }
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_token",
        "message": "유효하지 않은 token 입니다."
    }
}
```

#### 대기열에 없는 토큰 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_in_queue",
        "message": "해당 토큰으로 대기 중인 정보를 찾을 수 없습니다."
    }
}
```

~~## 예약 가능한 콘서트 이벤트 조회~~

특정 콘서트의 콘서트 이벤트 중 현재 예약 가능한 이벤트를 조회합니다.

### Endpoint

`GET` **/api/v1/concerts/{concertId}/available-events**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |
| User-Token    | string                | 예  | 대기열 진입 시 발급받은 토큰  |

### Request

#### Params

| 필드        | 타입     | 필수 | 설명     |
|-----------|--------|----|--------|
| concertId | number | 예  | 콘서트 ID |

#### Body

* 없음

### Response

list 의 element 는 아래와 같은 형태입니다.

| 필드   | 타입     | 설명                           |
|------|--------|------------------------------|
| id   | number | 콘서트 이벤트의 id                  |
| date | string | 예약 가능한 공연 날짜 (YYYY-MM-DD 형식) |

**예시**

```json
{
    "list": [
        {
            "id": 1,
            "date": "2023-07-01"
        },
        {
            "id": 2,
            "date": "2023-07-02"
        }
    ]
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_token",
        "message": "유효하지 않은 token 입니다."
    }
}
```

#### 콘서트를 찾을 수 없음 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_concert",
        "message": "요청한 콘서트를 찾을 수 없습니다."
    }
}
```

~~## 예약 가능한 좌석 조회~~

특정 콘서트 이벤트의 예약 가능한 좌석 목록을 조회합니다.

### Endpoint

`GET` **/api/v1/concert-events/{concertEventId}/available-seats**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |
| User-Token    | string                | 예  | 대기열 진입 시 발급받은 토큰  |

### Request

#### Params

| 필드             | 타입     | 필수 | 설명         |
|----------------|--------|----|------------|
| concertEventId | number | 예  | 콘서트 이벤트 ID |

#### Body

* 없음

### Response

list 의 element 는 아래와 같은 형태입니다.

| 필드         | 타입     | 설명    |
|------------|--------|-------|
| id         | number | 좌석 ID |
| seatNumber | string | 좌석 번호 |
| price      | number | 좌석 가격 |

**예시**

```json
{
    "list": [
        {
            "id": 1,
            "seatNumber": "12",
            "price": 50000
        },
        {
            "id": 2,
            "seatNumber": "15",
            "price": 50000
        }
    ]
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_token",
        "message": "유효하지 않은 token 입니다."
    }
}
```

#### 콘서트를 찾을 수 없음 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_concert",
        "message": "요청한 콘서트를 찾을 수 없습니다."
    }
}
```

#### 콘서트 이벤트를 찾을 수 없음 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_concert_event",
        "message": "요청한 콘서트 이벤트를 찾을 수 없습니다."
    }
}
```

~~## 좌석 점유~~

특정 콘서트 이벤트의 예약 가능한 좌석을 점유합니다.

### Endpoint

`POST` **/api/v1/concert-events/{concertEventId}/occupy**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |
| User-Token    | string                | 예  | 대기열 진입 시 발급받은 토큰  |

### Request

#### Params

| 필드             | 타입     | 필수 | 설명         |
|----------------|--------|----|------------|
| concertEventId | number | 예  | 콘서트 이벤트 ID |

#### Body

| 필드     | 타입     | 필수 | 설명    |
|--------|--------|----|-------|
| seatId | number | 예  | 좌석 ID |

**예시**

```json
{
    "seatId": 1
}
```

### Response

| 필드             | 타입     | 설명            |
|----------------|--------|---------------|
| id             | number | 점유한 좌석의 ID    |
| seatNumber     | string | 점유한 좌석의 좌석 번호 |
| price          | int    | 점유한 좌석의 가격    |
| expirationTime | string | 점유 만료 시간      |

**예시**

```json
{
    "data": {
        "id": 1,
        "seatNumber": "50",
        "price": 50000,
        "expirationTime": "2023-07-01T12:05:00Z"
    }
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_token",
        "message": "유효하지 않은 token 입니다."
    }
}
```

#### 유효하지 않은 콘서트 이벤트 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_date",
        "message": "예약할 수 없는 콘서트 이벤트입니다."
    }
}
```

### 이미 점유된 좌석 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_seat",
        "message": "이미 점유되었거나 예약된 좌석입니다."
    }
}
```

#### 콘서트 이벤트를 찾을 수 없음 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_concert_event",
        "message": "요청한 콘서트 이벤트를 찾을 수 없습니다."
    }
}
```

#### 좌석을 찾을 수 없음 (404 Not Found)

```json
{
    "error": {
        "code": "not_found_seat",
        "message": "콘서트 이벤트에서 해당 좌석을 찾을 수 없습니다."
    }
}
```

~~## 좌석 예약~~

점유한 좌석에 대해 예약 및 결제를 진행합니다.

### Endpoint

`POST` **/api/v1/concert-events/{concertEventId}/reserve**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |
| User-Token    | string                | 예  | 대기열 진입 시 발급받은 토큰  |

### Request

#### Body

| 필드     | 타입     | 필수 | 설명         |
|--------|--------|----|------------|
| seatId | number | 예  | 점유한 좌석의 ID |

**예시**

```json
{
    "seatId": 1234
}
```

### Response

| 필드             | 타입     | 설명         |
|----------------|--------|------------|
| id             | number | 예약의 ID     |
| concertEventId | number | 콘서트 이벤트 ID |
| totalSeats     | number | 총 예약한 좌석 수 |
| totalAmount    | number | 총 결제 금액    |
| createdAt      | string | 예약된 시간     |

**예시**

```json
{
    "data": {
        "id": 1,
        "concertEventId": 1,
        "totalSeats": 1,
        "totalAmount": 50000,
        "createdAt": "2023-07-01T12:05:00Z"
    }
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_token",
        "message": "유효하지 않은 token 입니다."
    }
}
```

### 잘못된 seat 정보 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_seat_status",
        "message": "다른 사용자가 점유한 좌석이거나 이미 예약된 좌석입니다."
    }
}
```

#### 잔액 부족 (400 Bad Request)

```json
{
    "error": {
        "code": "insufficient_balance",
        "message": "잔액이 부족합니다."
    }
}
```

#### 점유 시간 만료 (400 Bad Request)

```json
{
    "error": {
        "code": "occupation_expired",
        "message": "좌석 점유 시간이 만료되었습니다."
    }
}
```

~~## 결제 내역 목록 조회~~

사용자의 결제 내역 목록을 조회한다.

### Endpoint

`GET` **/api/v1/users/{userId}/payments**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |

### Request

#### Params

| 필드     | 타입     | 필수 | 설명     |
|--------|--------|----|--------|
| userId | number | 예  | 사용자 ID |

### Response

list 의 element 는 아래와 같은 형태입니다.

| 필드            | 타입     | 설명    |
|---------------|--------|-------|
| id            | number | 결제 ID |
| reservationId | number | 예약 ID |
| amount        | number | 결제 금액 |
| status        | string | 결제 상태 |
| paidAt        | string | 결제 시간 |

**예시**

```json
{
    "list": [
        {
            "id": 12,
            "reservationId": 5679,
            "amount": 60000,
            "status": "failed",
            "paidAt": "2023-07-02T14:20:00Z"
        },
        {
            "id": 13,
            "reservationId": 5679,
            "amount": 60000,
            "status": "success",
            "paidAt": "2023-07-02T14:30:00Z"
        }
    ]
}
```

~~## 잔액 조회~~

사용자의 현재 잔액 정보를 조회합니다.

### Endpoint

`GET` **/api/v1/users/{userId}/wallet**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |

### Request

#### Params

| 필드     | 타입     | 필수 | 설명     |
|--------|--------|----|--------|
| userId | number | 예  | 사용자 ID |

#### Body

* 없음

### Response

| 필드      | 타입     | 설명    |
|---------|--------|-------|
| balance | number | 현재 잔액 |

**예시**

```json
{
    "data": {
        "balance": 100000
    }
}
```

~~## 잔액 충전~~

사용자의 잔액을 충전합니다.

### Endpoint

`PATCH` **/api/v1/users/{userId}/wallet/charge**

### Headers

| 필드            | 값                     | 필수 | 설명                |
|---------------|-----------------------|----|-------------------|
| Authorization | Bearer {access_token} | 예  | 사용자 인증을 위한 액세스 토큰 |

### Request

#### Params

| 필드     | 타입     | 필수 | 설명     |
|--------|--------|----|--------|
| userId | number | 예  | 사용자 ID |

#### Body

| 필드     | 타입     | 필수 | 설명     |
|--------|--------|----|--------|
| amount | number | 예  | 충전할 금액 |

**예시**

```json
{
    "amount": 50000
}
```

### Response

| 필드      | 타입     | 설명     |
|---------|--------|--------|
| balance | number | 갱신된 잔액 |

**예시**

```json
{
    "data": {
        "balance": 150000
    }
}
```

### Error

#### 잘못된 요청 (400 Bad Request)

```json
{
    "error": {
        "code": "invalid_amount",
        "message": "유효하지 않은 충전 금액입니다."
    }
}
```