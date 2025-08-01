# modeni-app-be
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

### 🎯 맞춤 복지(문화정보) 추천 API

#### 1. 감정 일기 작성 (버튼 기반 자동 복지 추천 트리거)
```http
POST /api/diary
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "content": "오늘 스트레스가 많아서 힘들었다. 뭔가 새로운 활동을 해보고 싶다.",
  "emotionKeyword": "스트레스",
  "wishActivity": "독서하기"
}
```

**감정 키워드 옵션:**
- **긍정**: 행복, 뿌듯함, 즐거움, 설렘, 여유로움, 활기참, 안도감, 차분함, 기특함
- **부정**: 서운함, 불안함, 짜증남, 초조함, 실망, 후회, 우울함, 슬픔, 지침, 답답함

**희망 활동 옵션:**
- 산책하기, 요리하기, 청소하기, 독서하기, 그림그리기, 노래부르기, 카페가기, 일기쓰기, 운동하기, 사진찍기, 꽃구경, 잠자기, 영화보기, 맛집가기, 장보기, 음악듣기, 게임하기

#### 2. 맞춤 복지 추천 목록 조회
```http
GET /api/welfare/recommendations
Authorization: Bearer {JWT_TOKEN}
```

#### 3. 미읽음 추천 목록
```http
GET /api/welfare/recommendations/unread
Authorization: Bearer {JWT_TOKEN}
```

#### 4. 추천 클릭/신청 처리
```http
POST /api/welfare/recommendations/{id}/click
Authorization: Bearer {JWT_TOKEN}

POST /api/welfare/recommendations/{id}/apply
Authorization: Bearer {JWT_TOKEN}
```

#### 5. 직접 감정 분석 요청
```http
POST /api/welfare/analyze-emotion
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "text": "요즘 아이와 함께할 활동을 찾고 있어요. 책도 좋아하고 영어도 배우고 싶어해요."
}
```

#### 6. 버튼 기반 직접 추천 요청
```http
POST /api/welfare/recommend-by-buttons
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "emotionKeyword": "즐거움",
  "wishActivity": "독서하기"
}
```

#### 7. 🆕 GPT 기반 개인화된 추천 카드 생성
```http
POST /api/welfare/get-personalized-recommendations
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "emotionKeyword": "스트레스",
  "wishActivity": "그림그리기"
}
```

**🎯 성향 정보 있을 때 응답:**
```json
{
  "message": "개인화된 추천 카드를 생성 중입니다. GPT가 당신의 성향과 감정을 분석하여 특별한 추천 이유를 작성하고 있어요.",
  "userId": 1,
  "emotionKeyword": "스트레스",
  "wishActivity": "그림그리기",
  "personalityType": "남색이 (자율 독립형)",
  "hasPersonalityType": true,
  "recommendationMode": "고도화된 성향 기반 추천",
  "estimatedTime": "약 10-30초 후 추천 목록에서 확인 가능합니다."
}
```

**🔄 성향 정보 없을 때 응답:**
```json
{
  "message": "추천 카드를 생성 중입니다. GPT가 당신의 감정과 활동을 분석하여 추천 이유를 작성하고 있어요.",
  "userId": 1,
  "emotionKeyword": "스트레스", 
  "wishActivity": "그림그리기",
  "personalityType": "미설정",
  "hasPersonalityType": false,
  "recommendationMode": "감정 & 활동 기반 추천",
  "estimatedTime": "약 10-30초 후 추천 목록에서 확인 가능합니다."
}
```

**추천 카드 응답 형식:**
```json
{
  "id": 123,
  "title": "[꿈라이브러리]작사 작곡 아틀리에",
  "target": "초등 4~6학년 14명",
  "location": "4층 프로그램실",
  "schedule": "2025-07-27 ~ 2025-08-10 14:00 ~ 15:30 (일)",
  "applicationUrl": "https://lib.dongjak.go.kr/dj/module/teach/detail.do?...",
  "gptRecommendationReason": "현재 스트레스 상태에서 '그림그리기'에 대한 관심을 고려하여, 자율성과 독립성을 중시하는 성향에 맞는 작사 작곡 아틀리에를 추천드립니다. 이 프로그램은 개인의 창의성을 자유롭게 표현할 수 있는 공간을 제공하며, 스트레스 해소와 동시에 예술적 성취감을 얻을 수 있는 완벽한 기회입니다."
}
```

#### 8. 프로그램 검색
```http
GET /api/welfare/programs/search?keyword=독서&category=문화&age=10
Authorization: Bearer {JWT_TOKEN}
```

### 👤 성향 테스트 API

#### 1. 성향 타입 목록 조회
```http
GET /users/personality-types
```

#### 2. 사용자 성향 설정
```http
POST /users/{id}/personality
Content-Type: application/json

{
  "personalityType": "LOGICAL_BLUE"
}
```

**성향 타입 옵션:**
- **LOGICAL_BLUE**: 🧠 이성적 분석형 (파랑이) - 감정보다는 논리 중심, 갈등을 해결하려 함
- **EMOTIONAL_RED**: ❤️ 감정 공감형 (빨강이) - 정 교류 중시, 상처에도 예민
- **CONTROL_GRAY**: 👮‍♀️ 통제 보호형 (회색이) - 통제, 지도에 익숙하고 보호욕 강함
- **INDEPENDENT_NAVY**: 🕊 자율 독립형 (남색이) - 자기 선택을 중요시하고 간섭을 싫어함
- **AFFECTIONATE_YELLOW**: 🧸 애정 표현형 (노랑이) - 자주 표현하고 스킨십/말로 사랑을 전달
- **INTROSPECTIVE_GREEN**: 🔒 내면형 (초록이) - 표현은 적지만 속은 깊음, 혼자 해결하려 함

### 📚 감정 일기 API

#### 1. 일기 목록 조회
```http
GET /api/diary
Authorization: Bearer {JWT_TOKEN}
```

#### 2. 오늘 일기 확인
```http
GET /api/diary/today
Authorization: Bearer {JWT_TOKEN}
```

#### 3. 기간별 일기 조회
```http
GET /api/diary/range?startDate=2025-08-01&endDate=2025-08-31
Authorization: Bearer {JWT_TOKEN}
```

#### 4. 감정별 일기 검색
```http
GET /api/diary/search?emotion=행복
Authorization: Bearer {JWT_TOKEN}
```

### 🤖 OpenAI API

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

## 🧪 실제 테스트 시나리오

### 시나리오 1: 초등학생 자녀를 둔 부모의 문화 활동 추천

1. **사용자 설정**: 서울 거주, 35세 부모, 8세 자녀, **감정 공감형(빨강이)** 성향
2. **감정 일기 작성**:
   ```json
   {
     "content": "아이와 함께 할 수 있는 재미있는 활동을 찾고 있어요.",
     "emotionKeyword": "즐거움",
     "wishActivity": "독서하기"
   }
   ```
3. **AI 감정 분석 결과** (성향 반영):
   - 주요 감정: 긍정
   - 키워드: 즐거움, 독서, 교육, 학습, 문화, **가족, 소통, 만남** (성향 추가)
   - 추천 카테고리: 문화, 교육, 독서, **가족, 소통** (성향 추가)

4. **추천 결과** (성향 가중치 15% 추가 적용):
   - **"엄마와 함께하는 그림책 여행"** (가족 소통 키워드로 높은 점수)
   - **"[8월] 패밀리가 도서관에 떴다!"** (가족 활동으로 추가 가점)
   - "[초등 1,2]대학생 멘토와 10일의 기적!"
   - "제2회 여름 영어독서 캠프"
   - "원어민과 함께하는 스토리타임"

### 시나리오 2: 청소년의 스트레스 및 성장 관련 추천

1. **사용자 설정**: 서울 거주, 15세 청소년, **자율 독립형(남색이)** 성향
2. **감정 일기 작성**:
   ```json
   {
     "content": "요즘 공부 스트레스가 많고 진로에 대한 고민이 많아요.",
     "emotionKeyword": "스트레스",
     "wishActivity": "그림그리기"
   }
   ```
3. **AI 감정 분석 결과** (성향 반영):
   - 주요 감정: 부정 (스트레스)
   - 키워드: 스트레스, 그림그리기, 예술, 창작, 문화, 표현, **자율, 독립, 선택, 취미** (성향 추가)
   - 추천 카테고리: 문화, 예술, 창작, 상담, **취미, 개인활동** (성향 추가)

4. **추천 결과** (성향 가중치로 개인 활동 우선):
   - **"[꿈라이브러리]작사 작곡 아틀리에"** (개인 창작 활동으로 높은 점수)
   - **"까망돌 청소년 자원봉사단"** (자율적 선택 활동)
   - "청소년 생애주기별 프로그램《진로캠프 디딤돌》"
   - "[메이커 사업] 여름방학 메이커 캠프!" (창작 활동)

### 시나리오 3: 성인의 취미 및 평생학습 추천

1. **사용자 설정**: 서울 거주, 45세 성인, **이성적 분석형(파랑이)** 성향
2. **감정 일기 작성**:
   ```json
   {
     "content": "새로운 취미를 찾고 있어요. 독서도 좋아하고 역사에도 관심이 많아요.",
     "emotionKeyword": "뿌듯함",
     "wishActivity": "영화보기"
   }
   ```
3. **AI 감정 분석 결과** (성향 반영):
   - 주요 감정: 긍정 (뿌듯함)
   - 키워드: 뿌듯함, 영화보기, 문화, 여가, 감상, 체험, **분석, 논리, 교육, 학습** (성향 추가)
   - 추천 카테고리: 문화, 여가, 활동, **교육, 과학, 역사** (성향 추가)

4. **추천 결과** (논리적, 교육적 콘텐츠 우선):
   - **"요모조모 한국독립운동"** (역사+교육으로 최고 점수)
   - **"여름밤, OTT와 함께하는 격동의 한국현대사"** (역사 분석)
   - **"[지혜학교]언어로 인간을 읽다"** (논리적 학습)
   - "한강 작가 명작 3선 함께 읽기"
   - "이건범 작가와의 만남"

### 🆕 시나리오 4: GPT 기반 개인화된 추천 카드

1. **사용자 설정**: 서울 거주, 15세 청소년, **자율 독립형(남색이)** 성향
2. **추천받기 버튼 클릭**: "스트레스" + "그림그리기"
3. **GPT 개인화된 추천 이유 예시**:
   ```
   "현재 스트레스 상태에서 '그림그리기'에 대한 관심을 고려하여, 자율성과 독립성을 중시하는 성향에 맞는 작사 작곡 아틀리에를 추천드립니다. 이 프로그램은 개인의 창의성을 자유롭게 표현할 수 있는 공간을 제공하며, 스트레스 해소와 동시에 예술적 성취감을 얻을 수 있는 완벽한 기회입니다."
   ```

4. **추천 카드 상세 정보**:
   - **활동명**: [꿈라이브러리]작사 작곡 아틀리에
   - **대상**: 초등 4~6학년 14명  
   - **위치**: 4층 프로그램실
   - **기간**: 2025-07-27 ~ 2025-08-10 14:00 ~ 15:30 (일)
   - **실제 링크**: https://lib.dongjak.go.kr/dj/module/teach/detail.do?...
   - **GPT 설명**: 성향과 감정을 고려한 개인화된 추천 이유

### 🔄 시나리오 5: 성향 정보 없을 때의 적응형 추천

1. **사용자 설정**: 서울 거주, 25세 성인, **성향 미설정**
2. **추천받기 버튼 클릭**: "우울함" + "독서하기"
3. **시스템 응답**:
   ```json
   {
     "message": "추천 카드를 생성 중입니다. GPT가 당신의 감정과 활동을 분석하여 추천 이유를 작성하고 있어요.",
     "recommendationMode": "감정 & 활동 기반 추천",
     "hasPersonalityType": false
   }
   ```

4. **적응형 가중치 적용** (기본 매칭 모드):
   - **지역 매칭 30%** (서울 → 동작구 프로그램)
   - **연령 매칭 20%** (25세 성인 대상)
   - **감정 키워드 매칭 30%** (우울함, 독서 관련 키워드 강화)
   - **카테고리 매칭 20%** (독서, 문화, 교육 카테고리)

5. **GPT 추천 이유 (성향 없음)**:
   ```
   "현재 우울함 상태에서 '독서하기'에 대한 관심을 고려하여, 당신에게 도움이 될 수 있는 한강 작가 명작 3선 함께 읽기를 추천드립니다. 이 프로그램을 통해 원하시는 활동을 즐기며 긍정적인 변화를 경험하실 수 있을 것입니다."
   ```

**🎯 핵심 차이점:**
- **성향 있음**: 개인 특성 반영한 심화 분석 (20% 성향 가중치)
- **성향 없음**: 기본 요소(지역, 연령, 감정, 활동)에 집중한 안정적 추천

## 📊 데이터 현황

- **실제 동작구 도서관 문화 프로그램**: 95개
- **샘플 복지 프로그램**: 10개  
- **총 프로그램 수**: 105개
- **지원 지역**: 서울시 및 전국 주요 도시 (53개 지역)
- **대상 연령**: 유아(3세)부터 성인까지 전 연령

## 🔧 기술적 특징

### 🤖 AI 감정 분석
- OpenAI GPT-3.5-turbo를 활용한 고도화된 텍스트 분석
- **버튼 기반 감정 분석**: 18개 감정 키워드 + 17개 희망 활동 조합으로 정확한 추천
- **🆕 GPT 개인화된 추천 이유**: 사용자 성향 + 감정 + 프로그램 정보를 종합한 맞춤형 설명 생성
- 감정 분류, 키워드 추출, 추천 카테고리 자동 생성
- 실제 문화 프로그램 정보를 고려한 컨텍스트 분석

### 🎯 맞춤형 추천 알고리즘
**🔧 적응형 가중치 시스템**: 성향 정보 유무에 따른 동적 점수 조정

**성향 정보 있을 때 (고도화된 매칭):**
- **지역 매칭 (25%)**: 사용자 거주지 기반 필터링
- **연령 매칭 (15%)**: 대상 연령대 정확한 매칭  
- **감정 키워드 매칭 (25%)**: AI 분석 키워드와 프로그램 키워드 유사도
- **카테고리 매칭 (15%)**: 추천 카테고리 일치도
- **🆕 성향 매칭 (20%)**: 개인 성향과 프로그램 특성 매칭

**성향 정보 없을 때 (기본 매칭):**
- **지역 매칭 (30%)**: 지역 중심 강화
- **연령 매칭 (20%)**: 연령 적합성 강화
- **감정 키워드 매칭 (30%)**: 감정 분석 중심 강화
- **카테고리 매칭 (20%)**: 활동 카테고리 중심

### ⚡ 성능 최적화
- **비동기 처리**: 감정 일기 작성과 추천 생성 분리
- **데이터 캐싱**: 자주 조회되는 프로그램 정보 캐시
- **배치 처리**: 대량 데이터 로딩 및 분석 최적화

## 프로젝트 구조

```
src/main/java/com/steam/modeni/
├── config/          # 설정 클래스들 (OpenAI, 비동기 처리)
├── controller/      # REST API 컨트롤러들 (복지, 일기, OpenAI)
├── service/         # 비즈니스 로직 서비스들 (추천, 감정분석, CSV로더)
├── repository/      # 데이터 접근 계층 (복지프로그램, 추천결과, 일기)
├── domain/          # 도메인 엔티티들 (복지프로그램, 추천, 일기)
└── dto/            # 데이터 전송 객체들 (요청/응답, 감정분석결과)
```
