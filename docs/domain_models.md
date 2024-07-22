# 도메인 모델

## 핵심 개념 식별

### 엔티티

1. User: 사용자 정보를 관리
2. Wallet: 사용자의 잔액 정보를 관리
3. Transaction: 잔액 변동 내역을 기록
4. Concert: 콘서트의 기본 정보를 관리
5. ConcertEvent: 특정 날짜와 장소의 콘서트 이벤트를 관리
6. Seat: 콘서트 이벤트의 좌석 정보를 관리
7. SeatAllocation: 예약과 점유에 사용된 좌석의 할당 정보를 관리
8. Reservation: 좌석 예약 정보를 관리
9. Occupation: 좌석 임시 점유 정보를 관리
10. Payment: 결제 정보를 관리
11. QueueEntry: 대기열 정보를 관리

### 값 객체

1. DateTimeRange: start 부터 end 까지를 표현
2. TransactionType: CHARGE, PAYMENT
3. AllocationStatus: OCCUPIED, EXPIRED, RESERVED
4. ReservationStatus: PENDING, CONFIRMED, PAYMENT_FAILED
5. OccupationStatus: ACTIVE, EXPIRED, RELEASED
6. PaymentStatus: PENDING, COMPLETED, FAILED
7. QueueEntryStatus: READY, PROCESSING, EXITED, EXPIRED

## 바운디드 컨텍스트 및 애그리거트

### a. 사용자 관리 컨텍스트

- User 애그리게이트
    - 루트: User
    - 포함 엔티티: (없음)
    - 값 객체: (없음)

### b. 잔액 관리 컨텍스트

- Wallet 애그리게이트
    - 루트: Wallet
    - 포함 엔티티: Transaction
    - 값 객체: TransactionType

### c. 콘서트 관리 컨텍스트

- Concert 애그리게이트
    - 루트: Concert
    - 포함 엔티티: ConcertEvent, Seat
    - 값 객체: DateTimeRange, 

### d. 예약 및 점유 관리 컨텍스트

- Reservation 애그리게이트
    - 루트: Reservation
    - 포함 엔티티: (없음)
    - 값 객체: ReservationStatus
- Occupation 애그리게이트
    - 루트: Occupation
    - 포함 엔티티: SeatAllocation
    - 값 객체: OccupationStatus, AllocationStatus

### e. 결제 컨텍스트

- Payment 애그리게이트
    - 루트: Payment
    - 포함 엔티티: (없음)
    - 값 객체: PaymentStatus

### f. 대기열 관리 컨텍스트

- QueueEntry 애그리게이트
    - 루트: QueueEntry
    - 포함 엔티티: (없음)
    - 값 객체: QueueStatus

## 도메인 모델 다이어그램

```mermaid
classDiagram
    %% 사용자 관리 컨텍스트
    class User {
        +id
        +name
        +email
    }

    %% 잔액 관리 컨텍스트
    class Wallet {
        +id
        +userId
        +balance
    }
    class Transaction {
        +id
        +walletId
        +amount
        +type
    }
    class TransactionType {
        <<enumeration>>
        CHARGE
        PAYMENT
    }
    Wallet "1" -- "*" Transaction
    Transaction -- TransactionType

    %% 콘서트 관리 컨텍스트
    class Concert {
        +id
        +name
        +description
    }
    class ConcertEvent {
        +id
        +concertId
        +dateTimeRange
    }
    class Seat {
        +id
        +concertEventId
        +number
    }
    class DateTimeRange {
        +start
        +end
    }
    Concert "1" -- "*" ConcertEvent
    ConcertEvent "1" -- "*" Seat
    ConcertEvent -- DateTimeRange

    %% 예약 및 점유 관리 컨텍스트
    class Reservation {
        +id
        +userId
        +concertEventId
        +status
    }
    class ReservationStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        PAYMENT_FAILED
    }
    class Occupation {
        +id
        +userId
        +concertEventId
        +status
    }
    class SeatAllocation {
        +id
        +occupationId
        +seatId
        +status
    }
    class OccupationStatus {
        <<enumeration>>
        ACTIVE
        EXPIRED
        RELEASED
    }
    class AllocationStatus {
        <<enumeration>>
        OCCUPIED
        EXPIRED
        RESERVED
    }
    Reservation -- ReservationStatus
    Occupation "1" -- "*" SeatAllocation
    Occupation -- OccupationStatus
    SeatAllocation -- AllocationStatus

    %% 결제 컨텍스트
    class Payment {
        +id
        +reservationId
        +amount
        +status
    }
    class PaymentStatus {
        <<enumeration>>
        PENDING
        COMPLETED
        FAILED
    }
    Payment -- PaymentStatus

    %% 대기열 관리 컨텍스트
    class QueueEntry {
        +id
        +userId
        +concertEventId
        +status
    }
    class QueueEntryStatus {
        <<enumeration>>
        READY
        PROCESSING
        EXITED
        EXPIRED
    }
    QueueEntry -- QueueEntryStatus
```