# Modeni App Backend

가족 구성원을 연결해주는 정서 기반 복지 플랫폼의 백엔드 애플리케이션입니다.

## 기술 스택

- Spring Boot 3.5.4
- Java 17
- MySQL
- JWT 인증
- OpenAI API 통합

## 설정

### 환경 변수 설정

OpenAI API를 사용하기 위해 환경 변수를 설정해야 합니다:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

또는 `application.properties`에서 직접 설정할 수 있습니다:

```properties
openai.api.key=your-openai-api-key-here
```

## API 엔드포인트

### OpenAI API

#### 1. 간단한 채팅 응답 생성
```http
POST /api/openai/chat/simple
Content-Type: application/json

"안녕하세요, 오늘 날씨는 어떨까요?"
```

#### 2. 구조화된 채팅 요청
```http
POST /api/openai/chat
Content-Type: application/json

{
  "prompt": "안녕하세요, 오늘 날씨는 어떨까요?",
  "model": "gpt-3.5-turbo",
  "temperature": 0.7,
  "maxTokens": 1000
}
```

#### 3. 상세한 응답 정보
```http
POST /api/openai/chat/detailed
Content-Type: application/json

{
  "prompt": "안녕하세요, 오늘 날씨는 어떨까요?",
  "model": "gpt-3.5-turbo",
  "temperature": 0.7,
  "maxTokens": 1000
}
```

### 인증 API

#### 회원가입
```http
POST /auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "familyRole": "PARENT"
}
```

#### 로그인
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

## 실행 방법

1. MySQL 데이터베이스를 설정합니다.
2. 환경 변수를 설정합니다.
3. 애플리케이션을 실행합니다:

```bash
./gradlew bootRun
```

## 개발

### 빌드
```bash
./gradlew build
```

### 테스트
```bash
./gradlew test
```

## 프로젝트 구조

```
src/main/java/com/steam/modeni/
├── config/          # 설정 클래스들
├── controller/      # REST API 컨트롤러들
├── service/         # 비즈니스 로직 서비스들
├── repository/      # 데이터 접근 계층
├── domain/          # 도메인 엔티티들
└── dto/            # 데이터 전송 객체들
```