# 부하 테스트

## 테스트 대상 API 선정

- 대기열 Token 발급 API (POST /api/v1/queue/token)
- 대기열 정보 조회 API (GET /api/v1/queue/status)
- 좌석 점유 API (POST /api/v1/concert-events/{concertEventId}/occupy)
- 좌석 예약 API (POST /api/v1/concert-events/{concertEventId}/reserve)

## 테스트 목적

- 대규모 사용자가 동시에 접속할 때 시스템의 성능과 안정성 확인
- 대기열 시스템의 효율성 검증
- 좌석 점유 및 예약 과정에서의 동시성 처리 능력 평가

## 대기열 진입 부하 테스트 시나리오

- 목적: 대량의 사용자가 동시에 대기열에 진입할 때 시스템의 응답 시간과 처리량 확인
- 대상 API: 대기열 Token 발급 API (POST /api/v1/queue/token)
- 가상 사용자: 5,000명, 지속 시간: 5분

    ```jsx
    export const options = {
      // ... 다른 옵션 ...
        stages: [
            {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
            {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
            {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
        ]
    }
    ```

- SLI: 응답 시간, 처리량, 에러율
- SLO 를 k6 의 option 중 thresholds 로 작성

    ```jsx
    export const options = {
      // ... 다른 옵션 ...
      thresholds: {
        // 응답속도: 95%의 요청이 500ms 이내에 처리되어야 함
        http_req_duration: ['p(95)<500'],
        // 에러율: 전체 요청의 1% 미만이어야 함
        http_req_failed: ['rate<0.01'],
        // 처리량: 초당 최소 500개의 요청을 처리해야 함
        http_reqs: ['rate>500'],
      },
    };
    ```


### k6 스크립트

```jsx
import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

const URL_QUEUE_GEN_TOKEN = 'http://127.0.0.1:8080/api/v1/queue/token';
const COUNT_USER = 100000;
const basicHeader = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test::user::token'
    }
}

const getRandomUserId = () => Math.floor(Math.random() * COUNT_USER) + 1;

// 사용자 정의 지표 설정
const failureRate = new Rate('failed_requests');

// 테스트 설정
export const options = {
    stages: [
        {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
        {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
        {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95%의 요청이 1초 이내 처리
        http_req_failed: ['rate<0.01'], // 에러율 1% 미만
        http_reqs: ['rate>500'], // 초당 최소 500개 요청 처리
        checks: ['rate>0.999'], // 99.9% 이상의 요청 성공
    },
};

// 테스트 시나리오
export default function () {
    const userId = getRandomUserId();

    const payload = JSON.stringify({userId: userId});

    // API 호출
    const res = http.post(URL_QUEUE_GEN_TOKEN, payload, basicHeader);

    // 응답 확인
    const checkRes = check(res, {
        'status is 200': (r) => r.status === 200,
        'has token': (r) => r.json().data.token !== undefined,
        'has estimatedWaitingTime': (r) => r.json().data.estimatedWaitingTime !== undefined,
    });

    // 실패한 요청 기록
    failureRate.add(!checkRes);

    // 사용자 행동 시뮬레이션을 위한 대기 시간
    sleep(1);
}
```

### k6 수행 결과

![스크린샷 2024-08-23 오전 12.26.59.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/315950f2-a9dd-4897-a8e5-56c1bbdae102/3bc8d15a-e102-433b-b8fc-28f4fca054af/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2024-08-23_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_12.26.59.png)

```bash

          /\      |‾‾| /‾‾/   /‾‾/
     /\  /  \     |  |/  /   /  /
    /  \/    \    |     (   /   ‾‾\
   /          \   |  |\  \ |  (‾)  |
  / __________ \  |__| \__\ \_____/ .io

     execution: local
        script: test_generating_token.js
        output: Prometheus remote write (http://localhost:9090/api/v1/write)

     scenarios: (100.00%) 1 scenario, 5000 max VUs, 5m30s max duration (incl. graceful stop):
              * default: Up to 5000 looping VUs for 5m0s over 5 stages (gracefulRampDown: 30s, gracefulStop: 30s)

     ✓ status is 200
     ✓ has token
     ✓ has estimatedWaitingTime

   ✓ checks.........................: 100.00% ✓ 1669242     ✗ 0
     data_received..................: 193 MB  641 kB/s
     data_sent......................: 116 MB  384 kB/s
     failed_requests................: 0.00%   ✓ 0           ✗ 556414
     http_req_blocked...............: avg=9.06µs   min=0s    med=2µs      max=29.68ms p(90)=4µs      p(95)=6µs
     http_req_connecting............: avg=4.77µs   min=0s    med=0s       max=14.19ms p(90)=0s       p(95)=0s
   ✓ http_req_duration..............: avg=406.68ms min=576µs med=360.76ms max=1.9s    p(90)=875.82ms p(95)=910.85ms
       { expected_response:true }...: avg=406.68ms min=576µs med=360.76ms max=1.9s    p(90)=875.82ms p(95)=910.85ms
   ✓ http_req_failed................: 0.00%   ✓ 0           ✗ 556414
     http_req_receiving.............: avg=20.34µs  min=3µs   med=15µs     max=29.52ms p(90)=28µs     p(95)=38µs
     http_req_sending...............: avg=18.58µs  min=1µs   med=8µs      max=93.53ms p(90)=21µs     p(95)=38µs
     http_req_tls_handshaking.......: avg=0s       min=0s    med=0s       max=0s      p(90)=0s       p(95)=0s
     http_req_waiting...............: avg=406.64ms min=557µs med=360.69ms max=1.9s    p(90)=875.79ms p(95)=910.82ms
   ✓ http_reqs......................: 556414  1849.081407/s
     iteration_duration.............: avg=1.4s     min=1s    med=1.36s    max=2.9s    p(90)=1.87s    p(95)=1.91s
     iterations.....................: 556414  1849.081407/s
     vus............................: 1       min=1         max=5000
     vus_max........................: 5000    min=5000      max=5000

running (5m00.9s), 0000/5000 VUs, 556414 complete and 0 interrupted iterations
default ✓ [======================================] 0000/5000 VUs  5m0s
```

- 총 556,414번의 반복(iterations)이 완료되었고, 초당 약 1,849회의 요청을 처리했습니다.
- 모든 검사가 100% 성공했습니다. 총 1,669,242개의 검사가 수행되었고 실패는 없었습니다.
- 평균 응답 시간(http_req_duration): 406.68ms
- 95% 백분위 응답 시간: 910.85ms (이는 95%의 요청이 910.85ms 이내에 처리되었음을 의미합니다)
- 최대 응답 시간: 1.9초
- 모든 요청이 성공적으로 처리되었습니다 (http_req_failed: 0.00%)
- 임계값(Thresholds) 결과
    - http_req_duration: 통과 (95%의 요청이 1초 이내 처리됨)
    - http_req_failed: 통과 (에러율 0%, 1% 미만 목표 달성)
    - http_reqs: 통과 (초당 1,849개 요청 처리, 500개 이상 목표 달성)
    - checks: 통과 (100% 성공, 99.9% 이상 목표 달성)

## 대기열 정보 조회 부하 테스트 시나리오

- 목적: 대기열에 있는 사용자들이 지속적으로 상태를 확인할 때의 시스템 부하 확인
- 대상 API: 대기열 정보 조회 API (GET /api/v1/queue/status)
- 가상 사용자: 5,000명, 지속 시간: 5분

    ```jsx
    export const options = {
      // ... 다른 옵션 ...
        stages: [
            {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
            {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
            {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
        ]
    }
    ```

- SLI: 응답 시간, 처리량, 에러율
- SLO 를 k6 의 option 중 thresholds 로 작성

    ```jsx
    export const options = {
      thresholds: {
        // 응답속도: 99%의 요청이 500ms 이내에 처리되어야 함
        http_req_duration: ['p(99)<500'],
        // 에러율: 전체 요청의 0.5% 미만이어야 함
        http_req_failed: ['rate<0.005'],
        // 처리량: 초당 최소 1000개의 요청을 처리해야 함
        http_reqs: ['rate>1000'],
      },
    };
    ```


### k6 스크립트

```jsx
import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

// ------------------------------------------------------------------------
const URL_QUEUE_TOKEN = 'http://127.0.0.1:8080/api/v1/queue/token';
const URL_QUEUE_STATUS = 'http://127.0.0.1:8080/api/v1/queue/status';

const COUNT_USER = 100000;
const COUNT_TOKEN = 10000;

const basicHeader = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test::user::token'
    }
}

const getRandomUserId = () => Math.floor(Math.random() * COUNT_USER) + 1;
const makeUserTokenHeader = (userToken) => {
    return {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test::user::token',
            'User-Token': userToken
        }
    }
};

const requestGenerateToken = () => {
    const userId = getRandomUserId();
    const payload = JSON.stringify({userId: userId});
    const res = http.post(URL_QUEUE_TOKEN, payload, basicHeader);
    return res.json()
}
// ------------------------------------------------------------------------

// 사용자 정의 지표 설정
const failureRate = new Rate('failed_requests');

// 테스트 설정
export const options = {
    stages: [
        {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
        {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
        {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
    ],
    thresholds: {
        http_req_duration: ['p(99)<500'],
        http_req_failed: ['rate<0.005'],
        http_reqs: ['rate>1000'],
        checks: ['rate>0.9999'],
    },
    setupTimeout: '5m', // setup 에 충분한 시간 할당
};

// setup 함수에서 token 발급
export function setup() {
    // setup 함수에서 토큰 생성
    const generatedTokens = [];
    for (let i = 0; i < COUNT_TOKEN; i++) {
        const r = requestGenerateToken();
        if (r.data) {
            generatedTokens.push(r.data.token);
        }
    }

    console.log(`Generated ${generatedTokens.length} tokens`);

    // 생성된 토큰을 반환
    return {tokens: generatedTokens};
}

// 테스트 시나리오
export default function (data) {
    const {tokens} = data;
    // random token
    const token = tokens[Math.floor(Math.random() * tokens.length)];

    // api 호출
    const res = http.get(URL_QUEUE_STATUS, makeUserTokenHeader(token));

    const checkRes = check(res, {
        'status is 200': (r) => r.status === 200,
        'has queuePosition': (r) => r.json().data && r.json().data.position !== undefined,
        'has estimatedWaitingTime': (r) => r.json().data && r.json().data.estimatedWaitingTime !== undefined,
    });

    failureRate.add(!checkRes);

    sleep(1.5);
}
```

### k6 수행 결과

![스크린샷 2024-08-23 오전 1.10.39.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/315950f2-a9dd-4897-a8e5-56c1bbdae102/050eac16-8356-4915-a095-9e87a84286e7/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2024-08-23_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_1.10.39.png)

```bash
          /\      |‾‾| /‾‾/   /‾‾/
     /\  /  \     |  |/  /   /  /
    /  \/    \    |     (   /   ‾‾\
   /          \   |  |\  \ |  (‾)  |
  / __________ \  |__| \__\ \_____/ .io

     execution: local
        script: test_token_status.js
        output: Prometheus remote write (http://localhost:9090/api/v1/write)

     scenarios: (100.00%) 1 scenario, 5000 max VUs, 5m30s max duration (incl. graceful stop):
              * default: Up to 5000 looping VUs for 5m0s over 5 stages (gracefulRampDown: 30s, gracefulStop: 30s)

INFO[0009] Generated 10000 tokens                        source=console

     ✓ status is 200
     ✓ has queuePosition
     ✓ has estimatedWaitingTime

   ✓ checks.........................: 100.00% ✓ 1537737     ✗ 0
     data_received..................: 83 MB   266 kB/s
     data_sent......................: 195 MB  630 kB/s
     failed_requests................: 0.00%   ✓ 0           ✗ 512579
     http_req_blocked...............: avg=6.71µs  min=0s    med=2µs    max=12.33ms p(90)=4µs     p(95)=6µs
     http_req_connecting............: avg=3.65µs  min=0s    med=0s     max=11.81ms p(90)=0s      p(95)=0s
   ✓ http_req_duration..............: avg=28.06ms min=442µs med=2.15ms max=1.2s    p(90)=91.07ms p(95)=162.95ms
       { expected_response:true }...: avg=28.06ms min=442µs med=2.15ms max=1.2s    p(90)=91.07ms p(95)=162.95ms
   ✓ http_req_failed................: 0.00%   ✓ 0           ✗ 522579
     http_req_receiving.............: avg=15.66µs min=2µs   med=12µs   max=18.39ms p(90)=24µs    p(95)=32µs
     http_req_sending...............: avg=15.63µs min=1µs   med=6µs    max=33.8ms  p(90)=15µs    p(95)=26µs
     http_req_tls_handshaking.......: avg=0s      min=0s    med=0s     max=0s      p(90)=0s      p(95)=0s
     http_req_waiting...............: avg=28.03ms min=432µs med=2.12ms max=1.2s    p(90)=91.02ms p(95)=162.92ms
   ✓ http_reqs......................: 522579  1685.63326/s
     iteration_duration.............: avg=1.52s   min=1.5s  med=1.5s   max=8.67s   p(90)=1.59s   p(95)=1.66s
     iterations.....................: 512579  1653.377213/s
     vus............................: 3       min=0         max=5000
     vus_max........................: 5000    min=5000      max=5000

running (5m10.0s), 0000/5000 VUs, 512579 complete and 0 interrupted iterations
default ✓ [======================================] 0000/5000 VUs  5m0s
```

- 총 522,579개의 요청이 처리되었고, 초당 약 1,685개의 요청이 처리되었습니다.
- 평균 반복 시간(iteration_duration)은 1.52초입니다.
- 평균 응답 시간(http_req_duration)은 28.06ms입니다.
- 최소 응답 시간은 442µs, 최대 응답 시간은 1.2초입니다.
- 90%의 요청이 91.07ms 이내에, 95%의 요청이 162.95ms 이내에 처리되었습니다.
- 임계값(Thresholds) 결과
    - http_req_duration: 통과 (95%의 요청이 162.95ms 이내 처리됨, 500ms 미만 목표 달성)
    - http_req_failed: 통과 (에러율 0%, 0.5% 미만 목표 달성)
    - http_reqs: 통과 (초당 1,685개 요청 처리, 1,000개 이상 목표 달성)
    - checks: 통과 (100% 성공, 99.99% 이상 목표 달성)

## 좌석 점유 및 예약 프로세스 부하 테스트 시나리오

- 목적: 다수의 사용자가 동시에 좌석을 점유하고 예약할 때의 시스템 안정성 검증
- 대상 API:
    1. 좌석 점유 API (POST /api/v1/concert-events/{concertEventId}/occupy)
    2. 좌석 예약 API (POST /api/v1/concert-events/{concertEventId}/reserve)
- 가상 사용자: 5,000명, 지속 시간: 5분

    ```jsx
    export const options = {
      // ... 다른 옵션 ...
        stages: [
            {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
            {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
            {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
            {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
        ]
    }
    ```

- SLI: 응답 시간, 처리량, 에러율, 동시성 오류 발생 횟수
- SLO 를 k6 의 option 중 thresholds 로 작성

    ```jsx
    export const options = {
      thresholds: {
        // 응답속도: 90%의 요청이 2초 이내에 처리되어야 함
        http_req_duration: ['p(90)<2000'],
        // 에러율: 전체 요청의 0.1% 미만이어야 함
        http_req_failed: ['rate<0.001'],
        // 처리량: 초당 최소 100개의 요청을 처리해야 함
        http_reqs: ['rate>100'],
        // 가용성: 99.99% 이상의 요청이 성공해야 함
        checks: ['rate>0.9999'],
      },
    };
    ```


### k6 스크립트

```jsx
import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

// ------------------------------------------------------------------------
const URL_QUEUE_TOKEN = 'http://127.0.0.1:8080/api/v1/queue/token';
const URL_QUEUE_STATUS = 'http://127.0.0.1:8080/api/v1/queue/status';
const URL_CONCERT_EVENTS = "http://127.0.0.1:8080/api/v1/concert-events";

const COUNT_USER = 100000;
const COUNT_TOKEN = 10000;
const COUNT_CONCERT_EVENT = 50000;

const PRICE_SEAT = 10000.0

const basicHeader = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test::user::token'
    }
}

const getRandomUserId = () => Math.floor(Math.random() * COUNT_USER) + 1;
const getRandomConcertEventId = () => Math.floor(Math.random() * COUNT_CONCERT_EVENT) + 1;

const extractRandomElement = (list) => list[Math.floor(Math.random() * list.length)];

const makeUserTokenHeader = (userToken) => {
    return {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test::user::token',
            'User-Token': userToken
        }
    }
}

const generateTokens = (count) => {
    const userIds = new Set();
    const generatedTokens = [];
    for (let i = 0; i < count; i++) {
        const userId = getRandomUserId();

        const userIdsSize = userIds.size;
        userIds.add(userId);
        if (userIdsSize === userIds.size) continue; // 중복된 userId 는 또 token 을 발급하지 않도록

        const token = generateToken(userId);
        if (token) {
            generatedTokens.push(token);
        }
    }
    return generatedTokens
}

const generateToken = (userId) => {
    const payload = JSON.stringify({userId});
    const res = http.post(URL_QUEUE_TOKEN, payload, basicHeader);
    const body = res.json();

    if (!body.data) {
        console.log("fail generateToken", payload, body)
        return null;
    }

    return body.data.token;
}

const waitUntilActivation = (token) => {
    const headers = makeUserTokenHeader(token);

    let isActive = false;
    let retries = 0;

    while (!isActive && retries < 30) { // 최대 2.5분(5초 * 30) 대기
        const res = http.get(URL_QUEUE_STATUS, headers);
        const body = res.json();
        if (!body.data) {
            console.log("fail waitUntilActive", headers, body);
            sleep(5); // 5초 대기
            retries++;
            continue;
        }

        if (body.data.position === 0) {
            isActive = true;
        } else {
            sleep(5); // 5초 대기
            retries++;
        }
    }

    if (!isActive) {
        console.error('대기열에서 활성 토큰 되지 못함');
        return null;
    }
    return token;
}

const getAvailableSeatId = (token, concertEventId) => {
    const url = `${URL_CONCERT_EVENTS}/${concertEventId}/available-seats`;
    const headers = makeUserTokenHeader(token);
    const res = http.get(url, headers);
    const body = res.json();
    if (!body.list) {
        console.log("fail getAvailableSeatId", url, headers, body);
        return null;
    }
    return extractRandomElement(body.list).id;
}

const occupy = (token, concertEventId, seatId) => {
    const url = `${URL_CONCERT_EVENTS}/${concertEventId}/occupy`;
    const headers = makeUserTokenHeader(token);
    const payload = JSON.stringify({seatId})
    const res = http.post(url, payload, headers);
    const body = res.json();
    if (!body.data) {
        console.log("fail requestOccupy", url, payload, headers, body);
        return null
    }
    return body.data;
}

const requestReserveSeat = (token, concertEventId, occupationId) => {
    const url = `${URL_CONCERT_EVENTS}/${concertEventId}/reserve`;
    const payload = JSON.stringify({occupationId});
    const headers = makeUserTokenHeader(token)
    return http.post(url, payload, headers);
}
// -------------------------------------------------------------

const failureRate = new Rate('failed_requests');
const dataIntegrityRate = new Rate('data_integrity');

// 테스트 시나리오 및 SLO
export const options = {
    stages: [
        {duration: '1m', target: 1000}, // 1분 동안 1,000 명으로 증가
        {duration: '1m', target: 2000}, // 1분 동안 2,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명으로 증가
        {duration: '1m', target: 5000}, // 1분 동안 5,000 명 유지
        {duration: '1m', target: 0}, // 1분 동안 0 명으로 줄어듦
    ],
    thresholds: {
        http_req_duration: ['p(90)<2000'],
        http_req_failed: ['rate<0.001'],
        http_reqs: ['rate>100'],
        checks: ['rate>0.9999'],
        'data_integrity': ['rate==1.0'],
    },
};

// setup 함수에서 token 발급
export function setup() {
    const tokens = generateTokens(COUNT_TOKEN);
    console.log(`${tokens.length} token 생성`);
    return {tokens};
}

export default function (data) {
    const {tokens} = data;
    // random token
    const token = tokens[Math.floor(Math.random() * tokens.length)];

    if (!waitUntilActivation(token)) {
        console.log("token 이 활성화 되지 않아 테스트 종료")
        return;
    }

    const concertEventId = getRandomConcertEventId();

    let occupation = null;
    let attemptCount = 0;
    // 다른 사람이 먼저 점유할 수 있어, 재시도를 진행
    while (!occupation && attemptCount < 5) {

        const seatId = getAvailableSeatId(token, concertEventId);

        // 예약 가능한 좌석에 대해서 점유 시도
        occupation = occupy(token, concertEventId, seatId);

        if (!occupation) {
            sleep(1);  // 재시도 전 잠시 대기
            attemptCount++;
        }
    }

    if (!occupation) {
        console.log("모든 좌석 점유 시도 실패. 테스트를 종료");
        return;
    }

    const res = requestReserveSeat(token, concertEventId, occupation.id)
    const reserveCheck = check(res, {
        'reserve status is 200': (r) => r.status === 200,
        'seat reserved successfully': (r) => r.json().data && r.json().data.id !== undefined,
    });

    failureRate.add(!reserveCheck);

    // 데이터 무결성 확인
    if (reserveCheck) {
        const {totalSeats, totalAmount} = res.json().data
        dataIntegrityRate.add(totalSeats === 1 && Math.abs(totalAmount - PRICE_SEAT) < 1);
    }
    sleep(Math.random() * 5 + 5);
}
```

### k6 수행 결과

![스크린샷 2024-08-23 오전 2.54.22.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/315950f2-a9dd-4897-a8e5-56c1bbdae102/43fe8e9a-861e-4237-9348-1d3cccecb4ca/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2024-08-23_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_2.54.22.png)

```bash

     ✗ reserve status is 200
      ↳  99% — ✓ 30949 / ✗ 135
     ✗ seat reserved successfully
      ↳  99% — ✓ 30949 / ✗ 135

   ✗ checks.........................: 99.56%  ✓ 61898      ✗ 270
   ✓ data_integrity.................: 100.00% ✓ 30949      ✗ 0
     data_received..................: 108 MB  321 kB/s
     data_sent......................: 56 MB   167 kB/s
     failed_requests................: 0.43%   ✓ 135        ✗ 30949
     http_req_blocked...............: avg=12.55µs min=0s    med=1µs    max=12.47ms p(90)=4µs    p(95)=11µs
     http_req_connecting............: avg=9.02µs  min=0s    med=0s     max=12.43ms p(90)=0s     p(95)=0s
   ✗ http_req_duration..............: avg=4.52s   min=480µs med=2.04s  max=15.68s  p(90)=12.28s p(95)=13.21s
       { expected_response:true }...: avg=4.52s   min=480µs med=2.01s  max=15.68s  p(90)=12.28s p(95)=13.21s
   ✗ http_req_failed................: 0.12%   ✓ 175        ✗ 140590
     http_req_receiving.............: avg=27.94µs min=4µs   med=20µs   max=7.31ms  p(90)=48µs   p(95)=61µs
     http_req_sending...............: avg=11.6µs  min=2µs   med=9µs    max=3.63ms  p(90)=20µs   p(95)=26µs
     http_req_tls_handshaking.......: avg=0s      min=0s    med=0s     max=0s      p(90)=0s     p(95)=0s
     http_req_waiting...............: avg=4.52s   min=461µs med=2.04s  max=15.68s  p(90)=12.28s p(95)=13.21s
   ✓ http_reqs......................: 140765  417.306942/s
     iteration_duration.............: avg=26.3s   min=5.01s med=20.51s max=1m21s   p(90)=54.04s p(95)=56.7s
     iterations.....................: 30210   89.559498/s
     vus............................: 1       min=0        max=5000
     vus_max........................: 5000    min=5000     max=5000

running (5m37.3s), 0000/5000 VUs, 30210 complete and 2200 interrupted iterations
default ✓ [======================================] 0000/5000 VUs  5m0s
ERRO[0338] thresholds on metrics 'checks, http_req_duration, http_req_failed' have been crossed
```

- 평균 응답 시간은 4.52초로 높습니다.
- 90%의 요청이 12.28초 이내에 처리되었으며, 이는 목표인 2초를 크게 초과했습니다.
- 초당 약 417개의 요청을 처리했습니다.
- 오류율 (Error Rate)
    - 전체 요청 중 0.43%가 실패했습니다.
    - HTTP 요청 실패율은 0.12%로 목표인 0.1%를 약간 초과했습니다.
- 검사 (Checks)
    - 99.56%의 검사가 성공했지만, 목표인 99.99%에는 미치지 못했습니다.
    - '좌석 예약' 검사에서 30,949건의 성공과 135건의 실패가 있었습니다.
- 모든 성공적인 예약에 대해 데이터 무결성이 유지되었습니다 (100% 성공)
- 임계값(Thresholds) 결과
    - http_req_duration: 실패 (90%의 요청이 12.28초 이내 처리됨, 2초 미만 목표 실패)
    - http_req_failed: 실패 (에러율 0.12%, 0.1% 미만 목표 실패)
    - http_reqs: 통과 (초당 417개 요청 처리, 100개 이상 목표 달성)
    - checks: 실패 (99.56% 성공, 99.99% 이상 목표 실패)
    - data_integrity: 통과 (100% 성공, 100% 목표 달성)