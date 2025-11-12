# TimeWizard-be
장기프로젝트 3조의 백엔드 레포지토리입니다

# Bilnut 백엔드 구현 노트

## 🔐 인증/인가 (Spring Security + JWT)

### 사용 중인 필터
- **JwtAuthenticationFilter** (OncePerRequestFilter 상속)
    - Authorization 헤더에서 JWT 토큰 추출 (`Bearer {token}` 형식)
    - 토큰 유효성 검증 후 SecurityContext에 인증 정보 저장
    - UsernamePasswordAuthenticationFilter 앞에 배치

### JWT 토큰 구성
- **Access Token**: 1시간 유효, Authorization 헤더로 전송
- **Refresh Token**: 3일 유효, HttpOnly 쿠키로 전송, DB에 저장

### 주요 컴포넌트
```
SecurityConfig.java         → 필터 체인 설정 (STATELESS 세션)
JwtAuthenticationFilter.java → JWT 검증 및 인증 처리
JwtTokenProvider.java       → 토큰 생성/파싱/검증
CustomUserDetails.java      → 인증 객체 (userId + email + authorities)
```

### 인증 흐름
1. 로그인 → Access Token(응답) + Refresh Token(쿠키)
2. API 요청 시 Access Token을 Authorization 헤더에 포함
3. JwtAuthenticationFilter가 토큰 검증 후 인증 처리
4. 토큰 만료 시 `/auth/refresh`로 갱신

---

## 🤖 시간표 AI 생성 (Redis + Polling)

### Redis를 활용한 Transaction 관리

**UUID 기반 상태 저장**
- AI 생성 요청 시 UUID 생성
- Redis에 `{UUID: "WAITING"}` 형태로 저장 (TTL: 10분)
- 비동기 처리 완료 후 상태 업데이트: `COMPLETE` / `ERROR`

### Polling 방식 구현

```
[클라이언트]                     [백엔드]                    [AI 서버]
     |                              |                            |
     |-- POST /ai/generate -------->|                            |
     |                              |-- UUID 생성                |
     |                              |-- Redis: WAITING 저장      |
     |<----- UUID 반환 (202) -------|                            |
     |                              |-- @Async 비동기 호출 ----->|
     |                              |                            |
     |-- GET /check/{uuid} -------->|                            |
     |<--- {status: WAITING} -------|                            |
     |                              |                            |
     |-- GET /check/{uuid} -------->|                            |
     |<--- {status: WAITING} -------|                            |
     |                              |                            |
     |                              |<---- AI 응답 (JSON) -------|
     |                              |-- Redis: JSON 저장         |
     |                              |                            |
     |-- GET /check/{uuid} --------->|                           |
     |<--- {status: COMPLETE, data}--|                           |
```

### 주요 엔드포인트

**1. AI 생성 요청**
```http
POST /ai/generate-timetable
Content-Type: application/json

{
  "requestText": "월수금 수업 선호",
  "targetCredit": 18,
  "maxCredit": 21
}

Response: "a1b2c3d4-uuid-..." (202 ACCEPTED)
```

**2. 상태 확인 (Polling)**
```http
GET /check/{uuid}/status

Response:
{
  "status": "WAITING" | "COMPLETE" | "ERROR" | "NOT_FOUND",
  "message": "...",
  "data": null | {AI 응답 JSON}
}
```

### 비동기 처리 설정
- **@Async** + **ThreadPoolTaskExecutor**
- Core Pool Size: 5, Max Pool Size: 10
- WebClient (Reactive)로 AI 서버와 통신
- Connect Timeout: 5초, Read Timeout: 90초

### Redis 상태 관리
| 상태 | 의미 | Redis 값 |
|------|------|----------|
| WAITING | AI 처리 중 | `"WAITING"` |
| COMPLETE | 생성 완료 | `{AI JSON 응답}` |
| ERROR | 처리 실패 | `"ERROR"` |
| NOT_FOUND | 키 없음 (TTL 만료) | `null` |

---

## 🗄️ 데이터베이스 구조

### 핵심 엔티티
- **User**: 사용자 정보 (학교, 전공, 학년)
- **Timetable**: UUID를 PK로 사용 (Redis 키와 동일)
- **Course**: 강의 정보 (교과목 코드, 학점, 교수)
- **CourseTimes**: 강의 시간 (요일, 시작/종료 시간)
- **TimetableCourse**: 시간표-강의 매핑 (다대다 관계)
- **RefreshToken**: Refresh Token 저장

---

## 🛠 기술 스택

```
Spring Boot 3.5.7
├── Spring Security (JWT 인증)
├── Spring Data JPA (MySQL)
├── Spring Data Redis (Lettuce)
├── WebFlux (Reactive WebClient)
└── Springdoc OpenAPI (Swagger)

Database:
├── MySQL 8.0
└── Redis (로컬)
```

---

## 📝 주요 특징

1. **Stateless 인증**: JWT 기반, 세션 미사용
2. **비동기 처리**: @Async로 AI API 호출, 블로킹 없음
3. **Polling 패턴**: 클라이언트가 주기적으로 상태 확인
4. **Redis TTL**: 10분 후 자동 만료로 메모리 관리
5. **UUID 키**: AI 요청 추적 및 시간표 PK로 활용

