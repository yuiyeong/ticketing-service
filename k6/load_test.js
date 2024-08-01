import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

const BASE_URL = 'http://127.0.0.1:8080/api/v1';
const CHARGE_URL = (userId) => `${BASE_URL}/users/${userId}/wallet/charge`;
const QUEUE_GEN_TOKEN_URL = `${BASE_URL}/queue/token`;
const QUEUE_STATUS_URL = `${BASE_URL}/queue/status`;
const CONCERT_URL = `${BASE_URL}/concerts`;
const AVAILABLE_CONCERT_EVENTS_URL = (concertId) => `${CONCERT_URL}/${concertId}/available-events`;
const AVAILABLE_SEATS_URL = (concertEventId) => `${BASE_URL}/concert-events/${concertEventId}/available-seats`;
const OCCUPATION_URL = (concertEventId) => `${BASE_URL}/concert-events/${concertEventId}/occupy`;
const RESERVATION_URL = (concertEventId) => `${BASE_URL}/concert-events/${concertEventId}/reserve`;
const PAYMENT_URL = `${BASE_URL}/payments`;

const COUNT_USER = 20000;

const MAX_OCCUPY_ATTEMPTS = 3;  // 최대 점유 시도 횟수


const paramAsBasicHeader = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test::user::token'
    }
}

const paramAsUserTokenHeader = (userToken) => {
    return {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test::user::token',
            'User-Token': userToken
        }
    }
};

const getRandomUserId = () => Math.floor(Math.random() * COUNT_USER);

const is200StatusCode = (r) => r.status === 200;

const extractRandomElement = (list) => list[Math.floor(Math.random() * list.length)];

const checkStatus = (response, msg) => {
    let checker = {}
    checker[msg] = is200StatusCode
    check(response, checker) || errorRate.add(1)
};

const requestChargeWallet = (userId, amount) => {
    const payload = JSON.stringify({amount: amount});
    const response = http.patch(CHARGE_URL(userId), payload, paramAsBasicHeader);
    checkStatus(response, 'wallet charged successfully');
}

const requestGenToken = (userId) => {
    const payload = JSON.stringify({userId: userId});
    const response = http.post(QUEUE_GEN_TOKEN_URL, payload, paramAsBasicHeader);
    checkStatus(response, 'token generated successfully');
    const {token} = response.json().data
    return token
};

const requestUntilTokenIsActive = (token) => {
    let maxAttempts = 30;
    const interval = 3;
    const params = paramAsUserTokenHeader(token);
    while (maxAttempts > 0) {
        const response = http.get(QUEUE_STATUS_URL, params);
        checkStatus(response, 'token status is fetched successfully');

        if (response.json().data.position === 0) return true;

        maxAttempts--;

        sleep(interval)
    }

    return false
}

const requestConcerts = (token) => {
    const response = http.get(CONCERT_URL, paramAsUserTokenHeader(token));
    checkStatus(response, 'concerts are fetched successfully');
    const {list} = response.json();
    return extractRandomElement(list) // return random concert
}

const requestConcertEvents = (concertId, token) => {
    const response = http.get(AVAILABLE_CONCERT_EVENTS_URL(concertId), paramAsUserTokenHeader(token))
    checkStatus(response, 'concert events are fetched successfully');
    const {list} = response.json();
    return extractRandomElement(list) // return random concert event
}

const requestAvailableSeats = (concertEventId, token) => {
    const response = http.get(AVAILABLE_SEATS_URL(concertEventId), paramAsUserTokenHeader(token))
    checkStatus(response, 'available seats are fetched successfully');
    const {list} = response.json();

    if (list.length === 0) return null;

    return extractRandomElement(list) // return random seat
}

const requestOccupySeat = (concertEventId, seatId, token) => {
    const payload = JSON.stringify({seatId});
    const response = http.post(OCCUPATION_URL(concertEventId), payload, paramAsUserTokenHeader(token));

    const success = check(response, {
        'seat occupation is successful': (r) => r.status === 200,
    });

    if (success) {
        return response.json().data; // return occupation
    } else {
        return null;
    }
}

const requestReserveSeat = (concertEventId, occupationId, token) => {
    const payload = JSON.stringify({occupationId});
    const response = http.post(RESERVATION_URL(concertEventId), payload, paramAsUserTokenHeader(token));
    checkStatus(response, `Reservation is done successfully.`);
    const {data} = response.json();
    return data // return occupation
}

const requestPayReservation = (reservationId, token) => {
    const payload = JSON.stringify({reservationId});
    const response = http.post(PAYMENT_URL, payload, paramAsUserTokenHeader(token));
    checkStatus(response, `Payment is done successfully.`);
    const {data} = response.json();
    return data // return payment
}


export const options = {
    stages: [
        {duration: '1m', target: 100},
        {duration: '1m', target: 300},
        {duration: '1m', target: 500},
        {duration: '1m', target: 10},
        {duration: '1m', target: 0},
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'],
        'errors': ['rate<0.1']
    }
}

export const errorRate = new Rate('errors');

export default function () {
    const userId = getRandomUserId();

    // 잔액 충전
    requestChargeWallet(userId, 100000);
    sleep(1);

    // 토큰 발급
    const token = requestGenToken(userId);
    sleep(1);

    // 토큰이 활성 상태가 될 때까지 기다림
    requestUntilTokenIsActive(token);
    sleep(1);

    // 콘서트 목록 조회
    const concert = requestConcerts(token);
    sleep(1);

    // 해당 콘서트에서 예약 가능한 콘서트 이벤트 조회
    const concertEvent = requestConcertEvents(concert.id, token);
    sleep(1);

    let occupation = null;
    let attemptCount = 0;

    // 다른 사람이 먼저 점유할 수 있어, 재시도를 진행
    while (!occupation && attemptCount < MAX_OCCUPY_ATTEMPTS) {
        // 해당 콘서트 이벤트에서 예약 가능한 좌석 조회
        const seat = requestAvailableSeats(concertEvent.id, token);
        if (seat === null) {
            console.log("모든 좌석이 예약된 콘서트 이벤트로 테스트 종료.");
            return;
        }
        sleep(1);

        // 예약 가능한 좌석에 대해서 점유 시도
        occupation = requestOccupySeat(concertEvent.id, seat.id, token);
        if (!occupation) {
            console.log(`좌석 점유 실패. 다시 시도 (시도 횟수: ${attemptCount + 1})`);
            sleep(1);  // 재시도 전 잠시 대기
        }
        attemptCount++;
    }

    if (!occupation) {
        console.log("모든 좌석 점유 시도 실패. 테스트를 종료");
        return;
    }

    // 점유한 좌석에 대해서 예약
    const reservation = requestReserveSeat(concertEvent.id, occupation.id, token);
    sleep(1);

    // 예약에 대해 결제
    requestPayReservation(reservation.id, token);
}
