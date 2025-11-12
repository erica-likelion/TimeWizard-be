# TimeWizard-be
장기프로젝트 3조의 백엔드 레포지토리입니다

# Bilnut 백엔드 구현 노트

## 📝 주요 특징

1. **Stateless 인증**: JWT 기반, 세션 미사용
2. **비동기 처리**: @Async로 AI API 호출, 블로킹 없음
3. **Polling 패턴**: 클라이언트가 주기적으로 상태 확인
4. **Redis TTL**: 10분 후 자동 만료로 메모리 관리
5. **UUID 키**: AI 요청 추적 및 시간표 PK로 활용

## 🔐 인증/인가 (Spring Security + JWT)

### 사용 중인 필터
- **JwtAuthenticationFilter** (OncePerRequestFilter 상속)
    - Authorization 헤더에서 JWT 토큰 추출 (`Bearer {token}` 형식)
    - 토큰 유효성 검증 후 SecurityContext에 인증 정보 저장
    - UsernamePasswordAuthenticationFilter 앞에 배치

### JWT 토큰 구성
- **Access Token**: 1시간 유효, Authorization 헤더로 전송
- **Refresh Token**: 3일 유효, HttpOnly 쿠키로 전송, DB에 저장


### 인증 흐름
1. 로그인 → Access Token(응답) + Refresh Token(쿠키)
2. API 요청 시 Access Token을 Authorization 헤더에 포함
3. JwtAuthenticationFilter가 토큰 검증 후 인증 처리
4. 토큰 만료 시 `/auth/refresh`로 갱신

---

## 🤖 시간표 AI 생성 (Redis + Polling)

### 요즘 많이 사용되는 SSE 방식을 채택하지 않은 이유
요즘은 AI의 실시간성을 위해 SSE 방식을 사용하고, 작업물을 완성해달라고 요청하는 방식에서 완성도 (예를 들면 '10% 완성되었습니다') 표시를 위해 SSE를 많이 사용하는데요,
우리는 기획 단계에서 시간표 AI 제작이 실시간성도 필요없고 작업 완성도도 표시할 계획이 없었기 때문에 Polling 방식으로 AI 작업을 구현했습니다.


### Redis를 활용한 Transaction 관리

**UUID 기반 상태 저장**
- AI 생성 요청 시 UUID 생성
- Redis에 `{UUID: "WAITING"}` 형태로 저장 (TTL: 10분)
- 비동기 처리 완료 후 상태 업데이트: `COMPLETE` / `ERROR`

### Polling 방식 구현

```
[클라이언트]                     [백엔드]                    [AI 서버]
     |                               |                            |
     |-- POST /ai/generate --------> |                            |
     |                               |-- UUID 생성                |
     |                               |-- Redis: WAITING 저장      |
     |<----- UUID 반환 (202) ------- |                            |
     |                               |-- @Async 비동기 호출 ----->|
     |                               |                            |
     |-- GET /check/{uuid} --------> |                            |
     |<--- {status: WAITING} ------- |                            |
     |                               |                            |
     |-- GET /check/{uuid} --------> |                            |
     |<--- {status: WAITING} ------- |                            |
     |                               |                            |
     |                               |<---- AI 응답 (JSON) -------|
     |                               |-- Redis: JSON 저장         |
     |                               |                            |
     |-- GET /check/{uuid} --------> |                           |
     |<-{status: COMPLETE, AI 작업물}-|                           |
```

### 비동기 처리 설정
- **@Async** + **ThreadPoolTaskExecutor**
- Core Pool Size: 5, Max Pool Size: 10
- WebClient (Reactive)로 AI 서버와 통신
- Connect Timeout: 5초, Read Timeout: 90초


---

## 🗄️ 데이터베이스 구조

### 데이터베이스 스키마
<img width="912" height="790" alt="image" src="https://github.com/user-attachments/assets/167bcc44-0c83-494b-81de-4e6d50e8727d" />

- **User**: 사용자 정보 (학교, 전공, 학년)
- **Timetable**: UUID를 PK로 사용 (Redis 키와 동일)
- **Course**: 강의 정보 (교과목 코드, 학점, 교수)
- **CourseTimes**: 강의 시간 (요일, 시작/종료 시간)
- **TimetableCourse**: 시간표-강의 매핑 (다대다 관계)
- **RefreshToken**: Refresh Token 저장

### DB 설계를 하며 고민했던 점들
1. 처음에는 Course 테이블 컬럼에 강의 시간 데이터를 넣었다. 그런데 크롤링한 수강 편람 csv 파일을 import 해서 DB에 데이터를 넣으며 딸깍 너무 좋다~ 하다가 오류가 터졌다. 한 강의가 여러 요일의 강의 시간을 가지고 있는 경우를 고려하지 못해서 발생한 오류였다. 그래서 강의 시간과 강의가 일대다 관계를 가진다는걸 그제서야 알고 분리했다.
2. 단순 크롤링한 수강 편람 csv 파일을 AI에 전달해주면 답변 내용을 토대로 DB에서 조건문으로 어떤 강의를 선택한건지 찾아야했다. 그러나 모든 강의는 특정 강의에 유일하게 부여된 값이 없고 다 다른 데이터와 공유하는 컬럼만 존재했다. 심지어 강의 번호도 (ex. 22504) 중복되는 수업들이 존재했다. 그리고 강의는 5000개가 넘는지라 그걸 다 필터링하고 있기엔 동작이 무거웠다. 그래서 DB에서 PK를 부여하고 그 데이터를 csv 파일로 export 해서 AI와 DB가 가진 데이터가 서로 같은 pk를 공유하고 있게 만들었다. 그 다음부터는 AI 응답에 pk가 포함되어 있어서 수업 정보를 찾는 로직이 빨라졌다. 
3. AI 요청 후에 시간표가 마음에 든다면 클라이언트가 시간표에 이름을 부여하며 저장 API를 호출한다. 이때 AI 요청 트랜잭션에서 사용했던 UUID를 발행했기 때문에 Timetable의 PK 생성 전략을 다른 방식을 고안하기 보다 이때 만들었던 UUID를 그대로 부여하면 되겠다! 싶어서 Redis에서 사용했던 키 값을 그대로 DB PK로 사용했다.



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


