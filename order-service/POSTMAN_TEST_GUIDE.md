# 주문 서비스 포스트맨 테스트 가이드

## 서비스 포트 정보

| 서비스 | 포트 | Base URL |
|--------|------|----------|
| API Gateway | 8000 | `http://localhost:8000` |
| Discovery Service | 8761 | `http://localhost:8761` |
| User Service | 8100 | `http://localhost:8100` |
| Product Service | 8020 | `http://localhost:8020` |
| TimeDeal Service | 8030 | `http://localhost:8030` |
| Order Service | 8050 | `http://localhost:8050` |
| Payment Service | 8010 | `http://localhost:8010` |
| Queue Service | 8011 | `http://localhost:8011` |
| Auth Service | 9000 | `http://localhost:9000` |

---

## 테스트 데이터 생성 순서

### 0단계: 유저 생성 (User Service)
유저 서비스를 실행하면 포인트 10000원 있는 테스트 유저(더미 데이터)가 생기므로 애플리케이션만 실행하면 된다.
### 1단계: 상품 생성 (Product Service)

**POST** `http://localhost:8020/api/v1/products`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "userId": 1,
  "companyName": "나이키코리아",
  "productName": "후드집업",
  "description": "우먼스 기모 후드집업",
  "price": 119000,
  "category": "CLOTHES",
  "optionRequests": [
    {
      "size": "S",
      "color": "빨강"
    },
    {
      "size": "S",
      "color": "파랑"
    },
    {
      "size": "M",
      "color": "빨강"
    },
    {
      "size": "M",
      "color": "파랑"
    }
  ]
}
```

**응답 예시:**
```json
{
  "productName": "후드집업",
  "description": "우먼스 기모 후드집업",
  "price": 119000
}
```

**저장할 값:** `productId` (UUID)

---

### 2단계: 타임딜 생성 (TimeDeal Service)

**POST** `http://localhost:8030/api/v1/timedeals`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "title": "나이키 타임딜",
  "description": "20% 할인가 진행",
  "discountPrice": 95200,
  "limitQuantity": 5,
  "startAt": "2025-12-15T13:35:00Z",
  "endAt": "2025-12-30T23:59:59Z",
  "status": "IN_PROGRESS",
  "productId": "28c4825f-bead-4b77-aa56-099a92cbf406"
}
```

**응답 예시:**
```json
"3e3e330a-0708-4bb4-9505-fc8920be93ef"
```

**저장할 값:** `timeDealId` (UUID)

**참고:**
- `startAt`과 `endAt`은 미래 시간이어야 합니다 (ISO 8601 형식)
- 초기 `status`는 `SCHEDULED`

---

### 3단계: 재고 생성 (TimeDeal Service)

**POST** `http://localhost:8030/api/v1/stocks`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "productId": "360b4da6-b8a8-4741-b402-67cae9276f34", // 타임딜 상품 ID
  "totalStock": 100
}
```

**응답 예시:**
```json
{
  "stockId": "bf1a9907-b709-44a2-a834-f6944f80acfd",
  "productId": "8c08772a-0e44-4562-a4d2-198f174a34df", // 상품 ID가 아니라 타임딜 상품 ID가 들어가고 있음 -> DB에서 수동으로 상품ID 값으로 수정한 뒤 진행
  "availableStock": 100,
  "reservedStock": 0,
  "soldStock": 0,
  "totalStock": 100,
  "status": "AVAILABLE",
  "createdAt": "2025-12-15T13:39:02.8869433"
}
```

**저장할 값:** `stockId` (timeDealStockId로 사용, UUID)

---

### 4단계: 정책 생성 (Queue Service)

**POST** `http://localhost:8011/api/v1/queue/policies`

**Headers:**
```
Content-Type: application/json
X-User-Id: 2
X-User-Role: MASTER
```

**Body (JSON):**
```json
{
  "productId": "28c4825f-bead-4b77-aa56-099a92cbf406",
  "dealName": "신규 타임딜 이벤트",
  "status": "RUNNING",
  "startTime": "2025-12-15T13:35:00",
  "endTime": "2025-12-30T23:59:59",
  "maxCapacity": 3000,
  "limitSize": 100,
  "queueGap": 2,
  "ttl": 12000
}
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "policyId": "4158a95a-cf80-4351-8cd5-5a50263a8f6d",
    "productId": "28c4825f-bead-4b77-aa56-099a92cbf406",
    "dealName": "신규 타임딜 이벤트"
  },
  "timestamp": 1765774350991
}
```

---

### 5단계: 대기열 진입 요청 - 큐 토큰 발급 (Queue Service)

**POST** `http://localhost:8011/api/v1/queues/enter`

**Headers:**
```
Content-Type: application/json
X-User-Id: 1
X-User-Role: USER
```

**Body (JSON):**
```json
{
  "productId": "28c4825f-bead-4b77-aa56-099a92cbf406"
}
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "token": "f878f62c-2de4-4a0c-bb07-ab7ca343240d",
    "productId": "28c4825f-bead-4b77-aa56-099a92cbf406",
    "rank": 0,
    "status": "ACTIVE",
    "enteredAt": "2025-12-15T13:58:08.4617541",
    "message": "입장 가능합니다."
  },
  "timestamp": 1765774688462
}
```

**저장할 값:** `token` (예: `f878f62c-2de4-4a0c-bb07-ab7ca343240d`)

---

## 주문 서비스 API 테스트

### 테스트 1: 주문 생성 (Saga 시작)

**POST** `http://localhost:8050/api/v1/orders`

**Headers:**
```
Content-Type: application/json
X-User-Id: 1
X-User-Role: USER
X-Queue-Token: f878f62c-2de4-4a0c-bb07-ab7ca343240d
```

**Body (JSON):**
```json
{
  "timeDealId": "3e3e330a-0708-4bb4-9505-fc8920be93ef",
  "productId": "28c4825f-bead-4b77-aa56-099a92cbf406",
  "orderItems": [
    {
      "timeDealStockId": "bf1a9907-b709-44a2-a834-f6944f80acfd",
      "quantity": 2
    },
    {
      "timeDealStockId": "a7e5509a-c231-4660-8b25-0a22630a0148",
      "quantity": 2
    }
  ],
  "pointUsed": 5000,
  "shippingInfo": {
    "recipientName": "홍길동",
    "recipientPhone": "01012345678",
    "zipCode": "12345",
    "addressBase": "서울시 강남구 테헤란로",
    "addressDetail": "123번지 456호",
    "deliveryMessage": "문 앞에 놓아주세요"
  }
}
```

**예상 응답:**
```json
{
  "success": true,
  "data": {
    "sagaId": "9711079a-81c4-47bc-b5ff-c94a1e64e012",
    "status": "PROCESSING"
  },
  "timestamp": 1765776324537
}
```

**저장할 값:**
- `sagaId` (Saga 상태 조회용)
- 주문이 완료되면 `orderId`가 생성됨 (약간의 지연 후 조회 가능)

**검증:**
- 응답 상태 코드: `200 OK`
- `orderStatus`가 `PROCESSING`인지 확인
- `sagaId`가 반환되는지 확인

---
