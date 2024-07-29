# 콘서트 좌석 예약 서비스 시나리오 및 시퀀스 다이어그램

## 전제 조건

- 사용자는 이미 회원가입이 완료되어 있음
- 사용자는 서비스에 로그인한 상태임
- `잔액 관리`와 `User token 발급 API`를 제외한 나머지 시나리오에서는, User token 발급 API 로부터 받은 token 과 함께 API 를 호출한다.

## 1. 대기열 진입

### 1.1. 사용자가 User Token 발급 API 를 호출

- 1.1.1 대기열에 첫 진입일 경우, 새 Token 을 발행하고 User 를 대기열에 추가한 뒤, token 정보와 예상 대기 시간을 내려준다.
- 1.2.1 이미 Token 을 발급받았었고 대기열에 있는 경우, 아래 작업을 한 뒤, token 정보와 갱신된 예상 대기 시간을 내려준다.
    - 이전 Token 을 만료시키면서 대기열에서 제외
    - Token 을 재발행

```mermaid
sequenceDiagram
    actor User
    participant API as User Token API
    participant QueueService as Queue Service
    participant TokenManager as Token Manager
    User ->> API: Token 발급 요청
    API ->> QueueService: 사용자 대기 상태 확인
    alt 첫 진입
        QueueService ->> TokenManager: 새 Token 생성 요청
        TokenManager -->> QueueService: 새 Token
        QueueService ->> QueueService: User 를 대기열에 추가
        QueueService -->> API: Token 및 대기 정보
    else 이미 Token 발급 받음
        QueueService ->> TokenManager: 이전 Token 만료 요청
        TokenManager -->> QueueService: 만료 확인
        QueueService ->> QueueService: User 제거 후 재추가
        QueueService ->> TokenManager: 새 Token 생성 요청
        TokenManager -->> QueueService: 새 Token
        QueueService -->> API: 새 Token 및 갱신된 대기 정보
    end
    API -->> User: Token 및 예상 대기 시간
```

## 2. 대기열 정보 조회

### 2.1. 발급받은 Token 으로 대기 정보 조회 API 호출

- 2.1.1. 대기열에 있는 Token 이라면, 대기 정보와 갱신된 예상 대기 시간을 내려준다.
- 2.1.2. 대기열에 없는 Token 이라면, Error Response 를 내려준다.
- 2.1.3. 알 수 없는 사용자 정보로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Queue Info API
    participant QueueService as Queue Service
    participant TokenManager as Token Manager
    User ->> API: 대기 정보 조회 요청 (Token 포함)
    API ->> QueueService: 대기 정보 조회 (Token)
    QueueService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> QueueService: Token 유효 확인
        QueueService ->> QueueService: 대기 순서 및 예상 시간 계산
        QueueService -->> API: 대기 정보 (순서, 예상 시간)
        API -->> User: 대기 정보 및 갱신된 예상 대기 시간
    else Token 유효하지 않음
        TokenManager -->> QueueService: Token 유효하지 않음
        QueueService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    else Token이 대기열에 없음
        QueueService ->> QueueService: 대기열 확인
        QueueService -->> API: Error (대기열에 없음)
        API -->> User: Error Response (대기열에 없음)
    end
```

## 3. 예약 가능 정보 조회

### 3.0. 대기열 정보 조회시, 작업 진행 가능한 상태를 받은 상태

### 3.1. 한 콘서트의 예약 가능한 날짜 조회 API 호출

- 3.1.1. 예약 가능한 날짜 목록을 내려준다.
- 3.1.2. 만약 예약 가능한 날짜가 없다면, 빈 리스트를 내려준다.
- 3.1.3. 유효하지 않은 token 으로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.
- 3.1.4. 알 수 없는 콘서트 정보로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Reservation Info API
    participant ReservationService as Reservation Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    User ->> API: 예약 가능한 날짜 조회 요청 (Token, 콘서트ID)
    API ->> ReservationService: 예약 가능한 날짜 조회 (Token, 콘서트ID)
    ReservationService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> ReservationService: Token 유효 확인
        ReservationService ->> Repository: 콘서트 정보 요청 (콘서트ID)
        alt 콘서트 정보 존재
            Repository -->> ReservationService: 콘서트 정보
            ReservationService ->> ReservationService: 예약 가능 날짜 계산
            ReservationService -->> API: 예약 가능 날짜 목록
            API -->> User: 예약 가능 날짜 목록
        else 콘서트 정보 없음
            Repository -->> ReservationService: 콘서트 정보 없음
            ReservationService -->> API: Error (알 수 없는 콘서트)
            API -->> User: Error Response (알 수 없는 콘서트)
        end
    else Token 유효하지 않음
        TokenManager -->> ReservationService: Token 유효하지 않음
        ReservationService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    end
```

### 3.2. 한 콘서트의 예약 가능한, 특정 날짜로 예약 가능한 좌석 조회 API 호출

- 3.2.1. 예약 가능한 좌석 목록을 내려준다.
- 3.2.2. 만약 예약 가능한 좌석이 없다면, 빈 리스트를 내려준다.
- 3.2.3. 유효하지 않은 token 으로 호출한 경우, 해당 Error Response 를 내려준다.
- 3.2.4. 알 수 없는 콘서트 정보로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.
- 3.2.5. 예약 가능하지 않은 날짜로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Reservation Info API
    participant ReservationService as Reservation Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    User ->> API: 특정 날짜 좌석 조회 요청 (Token, 콘서트ID, 날짜)
    API ->> ReservationService: 특정 날짜 좌석 조회 (Token, 콘서트ID, 날짜)
    ReservationService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> ReservationService: Token 유효 확인
        ReservationService ->> Repository: 콘서트 및 좌석 정보 요청 (콘서트ID, 날짜)
        alt 콘서트 정보 존재 및 유효한 날짜
            Repository -->> ReservationService: 콘서트 및 좌석 정보
            ReservationService ->> ReservationService: 예약 가능 좌석 계산
            ReservationService -->> API: 예약 가능 좌석 목록
            API -->> User: 예약 가능 좌석 목록
        else 콘서트 정보 없음
            Repository -->> ReservationService: 콘서트 정보 없음
            ReservationService -->> API: Error (알 수 없는 콘서트)
            API -->> User: Error Response (알 수 없는 콘서트)
        else 유효하지 않은 날짜
            ReservationService -->> API: Error (예약 불가능한 날짜)
            API -->> User: Error Response (예약 불가능한 날짜)
        end
    else Token 유효하지 않음
        TokenManager -->> ReservationService: Token 유효하지 않음
        ReservationService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    end
```

## 4. 좌석 점유

### 4.1 특정 콘서트의, 예약 가능한 특정 날짜의, 예약 가능한 특정 좌석에 대해 좌석 점유 API 호출

- 4.1.1. 좌석 점유에 성공해서, 점유한 좌석의 정보와 점유 시간 제한(5분)을 내려준다.
- 4.1.2. 유효하지 않은 token 으로 호출한 경우, 해당 내용에 대응 되는 Error Response 를 내려준다.
- 4.1.3. 이미 점유 혹은 예약된 좌석이라서 실패한 경우, 해당 내용에 대응 되는 Error Response 를 내려준다.
- 4.1.4. 알 수 없는 콘서트 정보로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.
- 4.1.5. 예약 가능하지 않은 날짜로 호출한 경우, 해당 내용에 대응되는 Error Response 를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Seat Occupation API
    participant OccupationService as Occupation Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    User ->> API: 좌석 점유 요청 (Token, 콘서트ID, 날짜, 좌석ID)
    API ->> OccupationService: 좌석 점유 요청 (Token, 콘서트ID, 날짜, 좌석ID)
    OccupationService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> OccupationService: Token 유효 확인
        OccupationService ->> Repository: 콘서트 및 좌석 정보 요청 (콘서트ID, 날짜, 좌석ID)
        alt 콘서트 정보 존재 및 유효한 날짜
            Repository -->> OccupationService: 콘서트 및 좌석 정보
            OccupationService ->> OccupationService: 좌석 점유 가능 여부 확인
            alt 좌석 점유 가능
                OccupationService ->> Repository: 좌석 점유 요청 (5분)
                Repository -->> OccupationService: 점유 성공 확인
                OccupationService -->> API: 좌석 점유 성공 (점유 정보, 시간 제한)
                API -->> User: 좌석 점유 성공 (점유 정보, 시간 제한)
            else 이미 점유된 좌석
                OccupationService -->> API: Error (이미 점유된 좌석)
                API -->> User: Error Response (이미 점유된 좌석)
            end
        else 콘서트 정보 없음
            Repository -->> OccupationService: 콘서트 정보 없음
            OccupationService -->> API: Error (알 수 없는 콘서트)
            API -->> User: Error Response (알 수 없는 콘서트)
        else 유효하지 않은 날짜
            OccupationService -->> API: Error (예약 불가능한 날짜)
            API -->> User: Error Response (예약 불가능한 날짜)
        end
    else Token 유효하지 않음
        TokenManager -->> OccupationService: Token 유효하지 않음
        OccupationService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    end
```

## 5. 좌석 예약

### 5.1 사용자가 점유한 좌석에 대해 예약 API 호출

- 5.1.1. 예약에 성공하면, 아래 작업을 완료한 뒤, 예약 정보를 반환한다
- 5.1.2. 유효하지 않은 token 으로 호출한 경우, 해당 내용에 대응되는 Error Response 를 반환한다.
- 5.1.3. 좌석의 점유 시간이 만료되어 예약할 수 없는 경우, 해당 내용에 대응되는 Error Response 를 반환한다.
- 5.1.4. 이미 예약된 좌석인 경우, 해당 내용에 대응되는 Error Response 를 반환한다

```mermaid
sequenceDiagram
    actor User
    participant API as Reservation API
    participant ReservationService as Reservation Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    User ->> API: 예약 요청 (Token, 좌석 ID)
    API ->> ReservationService: 예약 요청 (Token, 좌석 ID)
    ReservationService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> ReservationService: Token 유효 확인
        ReservationService ->> Repository: 좌석 정보 조회
        Repository -->> ReservationService: 좌석 정보
        alt 예약 가능
            ReservationService ->> ReservationService: 예약 처리
            ReservationService -->> API: 예약 성공 (예약 정보)
            API -->> User: 예약 성공 (예약 정보)
        else 점유 시간 만료
            ReservationService -->> API: Error (점유 시간 만료)
            API -->> User: Error Response (점유 시간 만료)
        else 이미 예약된 좌석
            ReservationService -->> API: Error (이미 예약된 좌석)
            API -->> User: Error Response (이미 예약된 좌석)
        end
    else Token 유효하지 않음
        TokenManager -->> ReservationService: Token 유효하지 않음
        ReservationService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    end
```

## 6. 결제

### 6.1 사용자가 점유한 좌석에 대해 결제 API 호출

- 6.1.1. 잔액이 충분하여 결제 성공이 되면, 아래 작업을 완료한 뒤, 예약 정보를 내려준다.
    - 결제 내역을 만든다.
    - 해당 좌석의 소유권을 사용자에게 위임한다.
    - 대기열 token 을 만료시킨다.
- 6.1.2. 유효하지 않은 token 으로 호출한 경우, 해당 내용에 대응 되는 Error Response 를 내려준다.
- 6.1.3. 잔액이 부족하여 결제할 수 없는 경우, 해당 내용에 대응 되는 Error Response 를 내려준다.
- 6.1.4. 좌석의 점유 시간이 만료되어 결제할 수 없는 경우, 해당 내용에 대응 되는 Error Response 를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Payment API
    participant PaymentService as Payment Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    participant QueueService as Queue Service
    User ->> API: 결제 요청 (Token, 예약 ID)
    API ->> PaymentService: 결제 요청 (Token, 예약 ID)
    PaymentService ->> TokenManager: Token 유효성 검증
    alt Token 유효
        TokenManager -->> PaymentService: Token 유효 확인
        PaymentService ->> Repository: 예약 정보 및 사용자 잔액 조회
        Repository -->> PaymentService: 예약 정보 및 사용자 잔액
        PaymentService ->> PaymentService: 결제 가능 여부 확인 (잔액, 예약 상태)
        alt 결제 가능
            PaymentService ->> Repository: 결제 처리 요청
            Repository -->> PaymentService: 결제 처리 완료
            PaymentService ->> Repository: 예약 상태 변경
            Repository -->> PaymentService: 상태 변경 완료
            PaymentService ->> QueueService: 대기열 Token 만료 요청
            QueueService -->> PaymentService: Token 만료 완료
            PaymentService -->> API: 결제 성공 (결제 정보)
            API -->> User: 결제 성공 (결제 정보)
        else 잔액 부족
            PaymentService -->> API: Error (잔액 부족)
            API -->> User: Error Response (잔액 부족)
        else 유효하지 않은 예약
            PaymentService -->> API: Error (유효하지 않은 예약)
            API -->> User: Error Response (유효하지 않은 예약)
        end
    else Token 유효하지 않음
        TokenManager -->> PaymentService: Token 유효하지 않음
        PaymentService -->> API: Error (유효하지 않은 Token)
        API -->> User: Error Response (유효하지 않은 Token)
    end
```

### 6.2 사용자가 결제 내역 목록 API 호출

- 6.2.1. 사용자의 결제 내역 목록을 내려준다.
- 6.2.2. 만약 결제 내역이 없다면, 빈 리스트를 내려준다.

```mermaid
sequenceDiagram
    actor User
    participant API as Payment API
    participant PaymentService as Payment Service
    participant Repository as Repository
    User ->> API: 결제 내역 목록 요청
    API ->> PaymentService: 결제 내역 목록 요청
    PaymentService ->> Repository: 사용자 결제 내역 요청
    Repository -->> PaymentService: 결제 내역 목록
    PaymentService -->> API: 결제 내역 목록
    API -->> User: 결제 내역 목록
```

## 7. 잔액 관리

### 7.1 사용자가 잔액 조회 API 호출

- 7.1.1. 사용자의 현재 잔액 정보를 반환한다.

```mermaid
sequenceDiagram
    actor User
    participant API as Balance API
    participant WalletService as Wallet Service
    participant Repository as Repository
    User ->> API: 잔액 조회 요청
    API ->> WalletService: 잔액 조회 요청
    WalletService ->> Repository: 잔액 정보 조회
    Repository -->> WalletService: 현재 잔액 정보
    WalletService -->> API: 현재 잔액 정보
    API -->> User: 현재 잔액 정보
```

### 7.2 사용자가 잔액 충전 API 호출

- 7.2.1. 충전이 성공적으로 처리되면, 시스템이 충전 처리 후 갱신된 잔액 정보를 반환한다.

```mermaid
sequenceDiagram
    actor User
    participant API as Balance API
    participant WalletService as Wallet Service
    participant Repository as Repository
    User ->> API: 잔액 충전 요청
    API ->> WalletService: 잔액 충전 요청
    WalletService ->> Repository: 잔액 업데이트
    Repository -->> WalletService: 업데이트 완료
    WalletService -->> API: 갱신된 잔액 정보
    API -->> User: 갱신된 잔액 정보
```

## 8. 시스템 백그라운드 프로세스

### 8.1 대기열 관리

- 8.1.1. 시스템이 주기적으로 사용자의 최근 API 호출 시간을 확인한다.
- 8.1.2. 일정 시간이 경과한 사용자를 발견하면, 해당 사용자를 대기열에서 자동으로 제거한다.
- 8.1.3. 제거된 사용자의 token 을 무효화한다.

```mermaid
sequenceDiagram
    participant Scheduler as Scheduler
    participant QueueService as Queue Service
    participant TokenManager as Token Manager
    participant Repository as Repository
    participant Logger as Logger

    loop 주기적 실행
        Scheduler ->> QueueService: 대기열 관리 실행
        QueueService ->> Repository: 만료된 대기열 항목 일괄 조회
        Repository -->> QueueService: 만료된 대기열 항목 목록
        QueueService ->> TokenManager: Token 일괄 무효화 요청
        TokenManager -->> QueueService: Token 일괄 무효화 완료
        QueueService ->> Repository: 대기열에서 항목 일괄 제거
        Repository -->> QueueService: 일괄 제거 완료
        QueueService ->> Logger: 대기열 관리 결과 로그 기록
        alt 오류 발생
            QueueService ->> Logger: 오류 로그 기록
        end
    end
```

### 8.2 좌석 점유 만료 관리

- 8.2.1. 시스템이 주기적으로 점유된 좌석의 점유 시간을 확인한다.
- 8.2.2. 점유 시간이 만료된 좌석을 발견하면, 해당 좌석의 점유를 자동으로 해제한다.
- 8.2.3. 해제된 좌석을 다시 예약 가능한 상태로 변경한다.

```mermaid
sequenceDiagram
    participant Scheduler as Scheduler
    participant ReservationService as Reservation Service
    participant Repository as Repository
    participant Logger as Logger

    loop 주기적 실행
        Scheduler ->> ReservationService: 좌석 점유 만료 관리 실행
        ReservationService ->> Repository: 만료된 좌석 점유 일괄 조회
        Repository -->> ReservationService: 만료된 좌석 점유 목록
        ReservationService ->> Repository: 좌석 점유 일괄 해제 및 상태 변경
        Repository -->> ReservationService: 일괄 처리 완료
        ReservationService ->> Logger: 좌석 점유 만료 관리 결과 로그 기록
        alt 오류 발생
            ReservationService ->> Logger: 오류 로그 기록
        end
    end
```

### 8.3 예외 처리

- 8.3.1. 백그라운드 프로세스 실행 중 오류가 발생한 경우, 시스템이 오류 로그를 기록한다.