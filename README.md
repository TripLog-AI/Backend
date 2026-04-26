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

## 환경 변수 설정 (prod 프로파일)

`src/main/resources/application-prod.yml` 파일을 직접 만들거나,
루트에 `.env` 파일 생성 (`.gitignore`에 포함되어 있음):

```bash
cp .env.example .env
# .env 파일에 실제 값 입력
```

필요한 환경 변수 목록 → `docs/IMPLEMENTATION.md` 섹션 8 참고

## 프로젝트 구조

```
src/main/java/com/triple/travel/
├── common/          공통 (예외처리, DTO, 설정)
└── domain/
    ├── user/        회원
    ├── place/       장소 (Google Maps 캐시)
    ├── itinerary/   여행 일정 (핵심 도메인)
    ├── travelogue/  여행기 커뮤니티
    └── youtube/     유튜브 소스
```

## 현재 상태

- ✅ 도메인 엔티티 전체
- ✅ Repository (N+1 방지 쿼리 포함)
- ✅ 핵심 Service (Place Swap, 발행 스냅샷, 스크랩)
- ✅ Mock Controller (Swagger에서 바로 테스트 가능)
- 🔧 JWT 인증 (미구현)
- 🔧 Google Maps API 연동 (미구현)
- 🔧 AI 서비스 HTTP 클라이언트 (미구현)
