import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// ============================================
// 1. 커스텀 메트릭 정의 (결과 측정용)
// ============================================
const errorRate = new Rate('errors');
const enterQueueSuccess = new Rate('enter_queue_success');

// ============================================
// 2. 테스트 설정 (시나리오 정의)
// ============================================
export const options = {
  // ★ 수정 1: systemTags는 scenarios 밖으로 뺐습니다.
  // systemTags: ['status', 'method', 'url', 'name', 'group', 'check', 'error', 'iter', 'scenario', 'vu'],
  // scenarios: {
  //   // 시나리오: 타임딜 오픈 폭주
  //   timedeal_rush: {
  //     executor: 'ramping-vus',  // 점진적으로 사용자 증가
  //     startVUs: 0,              // 시작: 0명
  //     stages: [
  //       { duration: '10s', target: 100 },   // 10초 동안 100명까지 증가
  //       { duration: '20s', target: 500 },   // 20초 동안 500명까지 증가
  //       { duration: '30s', target: 1000 },  // 30초 동안 1000명까지 증가 (피크)
  //       { duration: '20s', target: 1000 },  // 20초 동안 1000명 유지
  //       { duration: '10s', target: 0 },     // 10초 동안 0명으로 감소 (종료)
  //     ],
  //     gracefulRampDown: '5s',   // 부드러운 종료
  //   },
  systemTags: ['status', 'method', 'url', 'name', 'group', 'check', 'error', 'iter', 'scenario', 'vu'],
  scenarios: {
    timedeal_rush: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 300 },    // 1분 동안 300명까지 워밍업
        { duration: '1m', target: 800 },    // 1분 동안 800명까지 증가
        { duration: '5m', target: 1000 },   // [핵심] 5분 동안 1000명 유지 (이때 스케일 아웃 발생!)
        { duration: '1m', target: 0 },      // 1분 동안 종료
      ],
      gracefulRampDown: '10s',
    },
  },

  // 임계값 설정 (테스트 실패 기준)
  thresholds: {
    'http_req_duration': ['p(95)<3000'],  // 95% 요청이 3초 이내
    'http_req_failed': ['rate<0.05'],     // 에러율 5% 미만
    'errors': ['rate<0.05'],
  },
};

// ============================================
// 3. 환경 변수 (수정 필요)
// ============================================
const BASE_URL = __ENV.BASE_URL || 'https://f2iy2uv1o3.execute-api.ap-northeast-2.amazonaws.com';
const PRODUCT_ID = __ENV.PRODUCT_ID || '1e76c9ef-ed5a-4707-9aa2-bc3c50693240';

// ============================================
// 4. JWT 토큰 생성 (간소화 버전)
// ============================================
function generateTestToken(userId) {
  // 실제로는 백엔드에서 발급받아야 하지만,
  // 테스트용으로 간단히 userId만 포함
  // TODO: 실제 JWT 발급 API 호출로 변경
  return `Bearer test-token-user-${userId}`;
}

// ============================================
// 5. 메인 테스트 함수 (각 VU가 실행)
// ============================================
export default function () {
  // 각 가상 유저(VU)는 고유한 userId를 가짐
  // const userId = __VU;  // Virtual User ID (1, 2, 3, ...)
  const timestamp = Date.now();
  const userId = timestamp + __VU;

  // JWT 토큰 생성
  const token = generateTestToken(userId);

  // ============================================
  // 테스트 1: 대기열 진입 요청
  // ============================================
  const enterQueuePayload = JSON.stringify({
    productId: PRODUCT_ID,
  });

  const enterQueueParams = {
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': `Bearer test-token-user-${userId}`,
      'X-User-Id': `${userId}`,
      'X-User-Role': 'USER',
      'X-User-Email': `user_${userId}@test.com`
    },
    timeout: '10s',  // 10초 타임아웃
    tags: { name: '01_Enter_Queue' },
  };

  const enterResponse = http.post(
    `${BASE_URL}/api/v1/queues/enter`,
    enterQueuePayload,
    enterQueueParams
  );

  // 409(이미 대기 중)는 에러로 치지 않기 위한 로직
  if (enterResponse.status === 409) {
    // 이미 대기열에 있다면, 실패가 아니라 "Pass"로 간주하고 루프 종료
    // (Polling 단계로 넘어갈 수도 있지만, 토큰이 없으므로 여기서 멈춤)
    return;
  }

  // ★ 수정 포인트: 응답 시간 체크를 분리했습니다.
  // 응답 시간이 1초 넘어도 201이면 일단 "기능적 성공"으로 칩니다.
  const isStatusOk = enterResponse.status === 201;

  // 응답 검증
// 성능 체크용 (로그만 찍거나 메트릭엔 반영하되 로직은 진행)
  check(enterResponse, {
    '응답 시간 < 2초': (r) => r.timings.duration < 2000,
  });

  const isTokenOk = check(enterResponse, {
    '대기열 진입 성공 (201)': (r) => r.status === 201,
    '토큰 발급됨': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.token;
      } catch {
        return false;
      }
    },
  });

  // 기능적으로 실패했을 때만 에러 처리
  if (!isStatusOk || !isTokenOk) {
    errorRate.add(1);
    console.error(`[VU ${userId}] 진입 실패: ${enterResponse.status} - ${enterResponse.body}`);
    return; // 진짜 실패했으니 여기서 종료
  }

  // 성공했으니 메트릭 기록하고 폴링으로 넘어감
  enterQueueSuccess.add(1);

  // 진입 실패 시 로그 출력
  // if (!enterSuccess) {
  //   console.error(`[VU ${userId}] 대기열 진입 실패: ${enterResponse.status} - ${enterResponse.body}`);
  // } else {
  //   // 성공 시 토큰 추출
  //
  // }

  let queueToken = null;

  try {
    // 1. 응답 본문이 비어있는지 먼저 확인
    if (!enterResponse.body) {
      throw new Error("응답 본문(Body)이 비어있습니다!");
    }
    // 2. 파싱 시도
    const responseBody = JSON.parse(enterResponse.body);
    // 3. 토큰 추출 시도
    if (responseBody.data && responseBody.data.token) {
      queueToken = responseBody.data.token;
    } else {
      // JSON은 맞는데 token 필드가 없는 경우
      throw new Error(`토큰 없음! 응답 구조 확인 필요: ${enterResponse.body}`);
    }

    const rank = responseBody.data.rank;

    console.log(`[VU ${userId}] 대기열 진입 성공! 순번: ${rank}, 토큰: ${queueToken.substring(0, 8)}...`);

    // ============================================
    // 테스트 2: 대기 순번 조회 (Polling 시뮬레이션)
    // ============================================
    sleep(1);  // 1초 대기 (실제 사용자가 기다리는 시간)

    const rankParams = {
      headers: {
        // 'Authorization': token,
        'X-Queue-Token': queueToken,
        'X-User-Id': `${userId}`,
        'X-User-Role': 'USER',
        'X-User-Email': `user_${userId}@test.com`
      },
      // ★ 여기가 핵심: 그래프에서 조회 요청은 따로 표시됨
      tags: { name: '02_Check_Rank' },
    };

    const rankResponse = http.get(
        `${BASE_URL}/api/v1/queues/rank?productId=${PRODUCT_ID}`,
        rankParams
    );

    const rankSuccess = check(rankResponse, {
      '순번 조회 성공 (200)': (r) => r.status === 200,
    });

    if (!rankSuccess) {
      // ★ 수정된 부분: rankRes -> rankResponse로 변수명 수정
      console.error(`[VU ${userId}] 조회 실패! Status: ${rankResponse.status} / Msg: ${rankResponse.body}`);
      errorRate.add(1);
    } else {
      console.log(`[VU ${userId}] 순번 조회 성공! 순번: ${rank}, 토큰: ${queueToken.substring(0, 8)}...`);
    }

  } catch (error) {
    console.error(`[VU ${userId}] 응답 파싱 실패: ${error}`);
    // 서버가 JSON 대신 뭘 보냈는지 눈으로 확인하는 로그
    console.error(`---------------------------------------------------`);
    console.error(`[VU ${userId}] 파싱/로직 에러 발생!`);
    console.error(`[에러 메시지]: ${error}`);
    console.error(`[서버 응답 상태]: ${enterResponse.status}`);
    console.error(`[서버 응답 본문]: ${enterResponse.body}`); // <--- 이걸 봐야 범인을 잡습니다.
    console.error(`---------------------------------------------------`);
    errorRate.add(1);
  }

  // 요청 간 간격 (1~3초 랜덤)
  sleep(Math.random() * 2 + 1);
}

// ============================================
// 6. 테스트 종료 후 요약 출력
// ============================================
export function handleSummary(data) {
  return {
    // 1. 콘솔에는 텍스트 요약 출력
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    // 2. 브라우저로 볼 수 있는 HTML 파일 생성 (이게 핵심!)
    'summary.html': htmlReport(data),
  };
}
