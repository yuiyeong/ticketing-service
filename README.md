# 콘서트 예약 서비스
- 특정 일시에 정원이 N 명인 콘서트의 좌석을 예약할 수 있는 서비스 만들기

## 요구사항
### 대기열 시스템:
- 사용자는 대기열 토큰을 발급받을 수 있어야 한다.
- 대기열 토큰은 대기 순서, 잔여 시간 등을 포함해야 한다.
- 사용자는 모든 작업을 하기 전, 대기열 토큰을 통해 작업 가능한지 검증을 받아야 한다.
- 기본적으로 폴링 방식을 사용하여 사용자가 자신의 대기열 상태를 확인할 수 있어야 한다.
- 유저 간 대기열을 요청 순서대로 정확하게 제공할 방법을 구현해야 한다.
- 유저 토큰 발급 API를 통해 구현되어야 한다.

### 잔액 관리:
- 사용자는 잔액을 충전할 수 있어야 한다.
- 사용자는 잔액을 조회할 수 있어야 한다.
- 좌석 예약 시에 미리 충전한 잔액을 이용해야 한다.
- 잔액 충전 / 조회 API를 구현해야 한다.

### 예약 시스템:
- 콘서트의 예약 가능한 날짜를 조회할 수 있어야 한다.
- 예약 가능한 날짜의 예약 가능한 좌석 목록을 조회할 수 있어야 한다.
- 좌석은 총 50개이며, 1번부터 50번까지 번호가 부여되어야 한다.
- 예약 가능한 날짜와 그 날짜의 좌석 번호를 가지고 예약을 할 수 있어야 한다.
- 예약 가능 날짜 / 좌석 조회 API를 구현해야 한다.
- 좌석 예약 요청 API를 구현해야 한다.

### 좌석 점유 및 결제:
- 사용자가 특정 날짜의 특정 좌석 번호로 예약을 시작하면, 일정 시간동안 점유할 수 있어야 한다.
- 좌석 점유 시간은 정책에 따라 변경될 수 있어야 한다.
- 특정 사용자가 좌석을 점유하고 있는 동안에, 다른 사용자는 그 좌석에 접근할 수 없어야 한다.
- 만약 일정 시간이 지나고도 점유된 좌석이 결제되지 않았다면, 모든 사용자가 예약할 수 있는 좌석이 되어야 한다.
- 결제가 완료되었다면, 결제 내역이 만들어져야 한다.
- 특정 사용자가 특정 좌석에 결제를 완료했다면, 해당 좌석의 예약자는 사용자가 되어야 한다.
- 사용자의 결제 완료 혹은 예약 활동이 종료되면, 대기열에서 빠져야 한다.
- 결제 API를 구현해야 한다.

### 시스템 요구사항:
- 다수의 인스턴스로 어플리케이션이 동작하더라도 기능에 문제가 없어야 한다.
- 동시성 이슈를 고려하여 구현해야 한다.


## 마일스톤

- [상세 마일스톤](./docs/milestone.md)

## 시나리오 및 시퀀스 다이어그램

- [상세 시나리오 및 시퀀스 다이어그램](./docs/scenario.md)

## API 명세

- [상세 API 명세](./docs/api_documents.md)

## 도메인 모델

- [상세 도메인 모델](./docs/domain_models.md)

## ERD

- [상세 erd](./docs/erd.md)