# TripLog AI — Backend

Java 21 / Spring Boot 3.x 기반 REST API 서버

## Quick Start (로컬 실행)

> JDK 21 이상 필요. DB 설정 없이 H2 인메모리로 즉시 실행 가능.

```bash
./mvnw spring-boot:run
```

| URL | 설명 |
|-----|------|
| `http://localhost:8080/swagger-ui.html` | API 명세 + 테스트 |
| `http://localhost:8080/h2-console` | DB 콘솔 (dev 전용) |

H2 Console 접속 정보:
- JDBC URL: `jdbc:h2:mem:tripledb`
- Username: `sa` / Password: (없음)

## 환경 변수 설정

루트에 `.env` 파일 생성 (`.gitignore` 포함됨):

```bash
cp .env.example .env
# 실제 값 입력 (JWT_SECRET, AI_SERVICE_URL, GOOGLE_MAPS_API_KEY 등)
```

전체 환경 변수 목록 → `Docs/IMPLEMENTATION.md` 섹션 8

## 인증 사용법

모든 보호 엔드포인트는 `Authorization: Bearer <accessToken>` 필요.

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"a@a.com","password":"password123","nickname":"alice"}'

# 2. 로그인 → accessToken 받기
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"a@a.com","password":"password123"}'

# 3. 보호 엔드포인트 호출
curl http://localhost:8080/api/v1/itineraries \
  -H "Authorization: Bearer <accessToken>"
```

Swagger UI 우상단 `Authorize` 버튼에 토큰 입력하면 모든 API 테스트 가능.

## 프로젝트 구조

```
src/main/java/com/triple/travel/
├── common/
│   ├── client/ai/        AI FastAPI RestClient
│   ├── client/google/    Google Maps Places 클라이언트
│   ├── config/           SecurityConfig, SwaggerConfig, DevDataSeeder
│   ├── security/         JWT (Provider, Filter, EntryPoint, Principal)
│   ├── exception/        커스텀 예외
│   └── handler/          GlobalExceptionHandler
└── domain/
    ├── auth/             회원가입/로그인
    ├── user/             회원
    ├── place/            장소 (Google Maps 캐시 + 검색 API)
    ├── itinerary/        여행 일정 (핵심)
    ├── travelogue/       여행기 커뮤니티
    └── youtube/          유튜브 추천 코스
```

## 현재 상태

- ✅ 전체 도메인 엔티티 + Repository (N+1 방지)
- ✅ JWT 인증 (signup/login/필터)
- ✅ 모든 Controller 실 service 연결 (Mock 제거 완료)
- ✅ AI 서비스 RestClient (`AiClient`) — AI 서버 없으면 즉시 FAILED
- ✅ Google Maps 클라이언트 골격 (api-key 없으면 DB 폴백)
- ✅ DevDataSeeder — 부팅 시 YouTube featured 코스 3개 자동 시드
- 🔧 Google Maps Places API 실제 호출 (`GoogleMapsClient.searchText` TODO)
- 🔧 소셜 로그인 (Google/Kakao OAuth2)
- 🔧 YouTube seed 코스 deep-copy
