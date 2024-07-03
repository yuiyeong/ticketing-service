# 콘서트 좌석 예매 서비스 ERD

```mermaid
erDiagram
    USER ||--o{ WALLET: has
    USER ||--o{ WALLET_TRANSACTION: makes
    USER ||--o{ PAYMENT: makes
    USER ||--o{ RESERVATION: makes
    USER ||--o{ WAITING_QUEUE: has
    WALLET ||--o{ WALLET_TRANSACTION: has
    CONCERT ||--o{ CONCERT_EVENT: has
    CONCERT_EVENT ||--o{ SEAT: has
    SEAT ||--o| USER_SEAT: occupied_by
    RESERVATION ||--|{ USER_SEAT: includes
    RESERVATION ||--|{ PAYMENT: has
    PAYMENT ||--|| WALLET_TRANSACTION: associated_with
    CONCERT ||--o{ RESERVATION: has
    CONCERT_EVENT ||--o{ RESERVATION: has
    USER {
        bigint id PK
        string name
    }
    WALLET {
        bigint id PK
        bigint user_id FK
        decimal balance
        datetime created_at
        datetime updated_at
    }
    WALLET_TRANSACTION {
        bigint id PK
        bigint user_id FK
        bigint wallet_id FK
        decimal amount
        string type
        datetime at
    }
    PAYMENT {
        bigint id PK
        bigint user_id FK
        bigint wallet_transaction_id FK
        bigint reservation_id FK
        decimal amount
        string status
        string failure_reason
        datetime created_at
    }
    CONCERT {
        bigint id PK
        string title
        string singer
        string description
    }
    CONCERT_EVENT {
        bigint id PK
        bigint concert_id FK
        string venue
        datetime reservation_start_at
        datetime reservation_end_at
        datetime start_at
        int duration
        int max_seat_count
        int available_seat_count
    }
    SEAT {
        bigint id PK
        bigint concert_event_id FK
        string seat_number
        decimal price
        string status
        datetime occupied_at
        datetime expired_at
    }
    USER_SEAT {
        bigint id PK
        bigint seat_id FK
        bigint reservation_id FK
        decimal seat_price
        datetime created_at
    }
    RESERVATION {
        bigint id PK
        bigint user_id FK
        bigint concert_id FK
        bigint concert_event_id FK
        int total_seats
        decimal total_amount
        datetime created_at
    }
    WAITING_QUEUE {
        bigint id PK
        bigint user_id FK
        string token
        string status
        int queue_position
        datetime created_at
        datetime expired_at
    }
```

## USER

- 사용자를 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - name: 이름

## WALLET

- 사용자의 잔액을 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - user_id: USER 를 가리키는 FK
    - balance: 잔액
    - created_at: 생성일시
    - updated_at: 수정일시

## WALLET_TRANSACTION

- 사용자의 잔액 변화 내역을 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - user_id: USER 를 가리키는 FK
    - wallet_id: WALLET 을 가리키는 FK
    - amount: 변화된 금액
    - type: 내역의 종류 ('charged' 또는 'used')
    - at: 변화 일시

## PAYMENT

- 사용자의 결제 내역을 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - user_id: USER 를 가리키는 FK
    - wallet_transaction_id: WALLET_TRANSACTION 을 가리키는 FK
    - reservation_id: RESERVATION 을 가리키는 FK
    - status: 결제 상태 ('pending', 'success', 'failed')
    - amount: 결제 금액
    - failure_reason: 실패 이유로, status 가 failed 때만 값이 있음
    - created_at: 결제 일시

## CONCERT

- 콘서트의 핵심 정보를 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - title: 콘서트 제목
    - singer: 가수
    - description: 설명

## CONCERT_EVENT

- 실제 열리는 콘서트의 정보를 나타내는 Table 이다
- 아래의 attributes 를 갖는다.
    - id: PK
    - concert_id: CONCERT 를 가리키는 FK
    - venue: 콘서트 장소
    - reservation_start_at: 예약 시작 시간
    - reservation_end_at: 예약 종료 시간
    - start_at: 공연 시작 일시
    - duration: 공연 시간(분 단위)
    - max_seat_count: 최대 좌석 수
    - available_seat_count: 사용 가능한 좌석 수

## SEAT

- 실제 열리는 콘서트의 좌석 정보를 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - concert_event_id: CONCERT_EVENT 를 가리키는 FK
    - seat_number: 좌석 번호
    - price: 가격
    - status: 상태 ('available', 'occupied', 'reserved')
    - occupied_at
    - reserved_at

## USER_SEAT

- 사용자가 예약한 좌석 정보를 나타내는 Table 이다
- SEAT 과 RESERVATION 을 연결하기 위한 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - seat_id: SEAT 를 가리키는 FK
    - reservation_id: RESERVATION 을 가리키는 FK
    - seat_price: 좌석 가격
    - created_at: 생성 일시

## RESERVATION

- 사용자의 좌석 예약 정보를 나타내는 Table 이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - user_id: USER 를 가리키는 FK
    - concert_id: CONCERT 를 가리키는 FK
    - concert_event_id: CONCERT_EVENT 를 가리키는 FK
    - total_seats: 총 예약 좌석 수
    - total_amount: 총 예약 금액
    - created_at: 예약 생성 시간

## WAITING_QUEUE

- 대기열을 나타내는 Table 이다.
- 활성 상태('ready', 'processing')인 row 의 수가 대기열의 사이즈이다.
- 아래의 attributes 를 갖는다.
    - id: PK
    - user_id: USER 를 가리키는 FK
    - token: 대기표 토큰
    - status: 상태 ('ready', 'processing', 'exited', 'expired')
    - queue_position: 대기열에서의 위치
    - created_at: 생성 시간
    - expired_at: 만료 시간

