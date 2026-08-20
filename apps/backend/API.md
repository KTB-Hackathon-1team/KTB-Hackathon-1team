# Backend API 명세

현재 구현된 부모 인증 API 기준 문서입니다.

## 기본 정보

- 로컬 Base URL: `http://localhost:8080`
- 프론트 로컬 주소: `http://localhost:5173`
- 모든 요청과 응답은 JSON을 사용합니다.
- Refresh Token은 응답 body가 아닌 `HttpOnly` 쿠키로 전달됩니다.

## 공통 응답 형식

성공 응답은 `CommonResponse<T>` 형식을 사용합니다.

```json
{
  "message": "응답 메시지",
  "data": {}
}
```

데이터가 없는 응답은 다음과 같습니다.

```json
{
  "message": "로그아웃 성공",
  "data": null
}
```

## 인증 토큰 정책

| 항목 | 값 |
| --- | --- |
| Access Token 만료 시간 | 30분 |
| Refresh Token 만료 시간 | 14일 |
| Access Token 전달 방식 | 응답 body |
| Refresh Token 전달 방식 | `HttpOnly` 쿠키 |
| Refresh Token 쿠키 이름 | `refresh_token` |
| Refresh Token 쿠키 경로 | `/api/auth` |
| Refresh Token 저장 방식 | SHA-256 해시만 DB 저장 |
| 부모 계정당 Refresh Token | 1개 |

프론트에서는 Refresh Token을 직접 읽을 수 없습니다. `document.cookie`로 접근하지 말고, 요청에 `credentials: "include"`를 설정해야 합니다.

## 1. 회원가입

### Request

```http
POST /api/auth/signup
Content-Type: application/json
```

```json
{
  "loginId": "parent01",
  "password": "password123",
  "nickname": "홍길동"
}
```

### Response

상태 코드: `201 Created`

응답과 함께 `refresh_token` 쿠키가 발급됩니다.

```json
{
  "message": "회원가입 성공",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "loginId": "parent01",
      "nickname": "홍길동",
      "role": "PARENT"
    }
  }
}
```

회원가입 성공 시 자동으로 로그인 처리됩니다.

## 2. 로그인

### Request

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "loginId": "parent01",
  "password": "password123"
}
```

### Response

상태 코드: `200 OK`

```json
{
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "loginId": "parent01",
      "nickname": "홍길동",
      "role": "PARENT"
    }
  }
}
```

로그인할 때 기존 Refresh Token이 있으면 삭제되고 새 Refresh Token이 발급됩니다.

## 3. Access Token 갱신

### Request

```http
POST /api/auth/refresh
```

Request Body는 없습니다. 브라우저가 `refresh_token` 쿠키를 함께 보내야 합니다.

### Response

상태 코드: `200 OK`

```json
{
  "message": "refresh token 성공",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "loginId": "parent01",
      "nickname": "홍길동",
      "role": "PARENT"
    }
  }
}
```

Refresh Token은 갱신 시 교체되지 않고 기존 쿠키를 계속 사용합니다.

## 4. 로그아웃

### Request

```http
POST /api/auth/logout
```

Request Body는 없습니다. 쿠키가 있으면 서버에서 해당 Refresh Token을 삭제합니다.

### Response

상태 코드: `200 OK`

```json
{
  "message": "로그아웃 성공",
  "data": null
}
```

서버에서 Refresh Token을 삭제하고 쿠키를 만료시킵니다. 프론트에서도 저장 중인 Access Token을 삭제해야 합니다.

## Access Token 사용법

인증이 필요한 API 요청에는 `Authorization` 헤더를 추가합니다.

```http
Authorization: Bearer {accessToken}
```

TypeScript `fetch` 예시:

```ts
const response = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include",
  body: JSON.stringify({
    loginId: "parent01",
    password: "password123",
  }),
});

const result = await response.json();
const accessToken = result.data.accessToken;
```

인증 API와 Refresh Token 쿠키를 사용하는 요청에는 다음 옵션을 포함합니다.

```ts
credentials: "include"
```

## Access Token 만료 처리

1. 인증 API 요청에서 `401 Unauthorized` 응답을 받습니다.
2. `POST /api/auth/refresh`를 `credentials: "include"`로 호출합니다.
3. 성공하면 응답의 새 Access Token을 저장합니다.
4. 실패하면 로그인 화면으로 이동합니다.

## 5. 아이 프로필 목록 조회

로그인한 부모가 본인이 등록한 아이 프로필 목록을 조회합니다. 생성된 순서대로 반환하며, 등록된 아이가 없으면 빈 배열을 반환합니다.

### Request

```http
GET /api/children
Authorization: Bearer {accessToken}
```

### Response

상태 코드: `200 OK`

```json
{
  "message": "아이 프로필 목록 조회 성공",
  "data": [
    {
      "id": 1,
      "name": "민준",
      "birthDate": "2016-05-12",
      "gender": "MALE",
      "profileImageUrl": null
    }
  ]
}
```

## 6. 아이 프로필 생성

로그인 후 발급받은 Access Token이 필요한 API입니다. JWT의 부모 계정 ID를 기준으로 아이 프로필의 부모가 자동 연결됩니다.

### Request

```http
POST /api/children
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "name": "민준",
  "birthDate": "2016-05-12",
  "gender": "MALE"
}
```

`gender`는 `MALE` 또는 `FEMALE`만 허용합니다.

### Response

상태 코드: `201 Created`

```json
{
  "message": "아이 프로필 생성 성공",
  "data": {
    "id": 1,
    "name": "민준",
    "birthDate": "2016-05-12",
    "gender": "MALE",
    "profileImageUrl": null
  }
}
```

## 7. 아이 프로필 이미지 업로드

로그인한 부모가 본인이 소유한 아이 프로필에 이미지를 업로드합니다. 이미지 파일은 S3에 저장되고, DB에는 S3 객체 키만 저장됩니다.

### Request

```http
POST /api/children/{childProfileId}/profile-image
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

Postman의 `form-data`에서 다음 항목을 추가합니다.

| Key | Type | 설명 |
| --- | --- | --- |
| `file` | File | JPEG, PNG, WebP 이미지, 최대 5MB |

### Response

상태 코드: `200 OK`

```json
{
  "message": "프로필 이미지 업로드 성공",
  "data": {
    "id": 1,
    "name": "민준",
    "birthDate": "2016-05-12",
    "gender": "MALE",
    "profileImageUrl": "https://s3-presigned-url..."
  }
}
```

`profileImageUrl`은 private S3 객체를 읽기 위한 Presigned URL이며 10분 후 만료됩니다. 같은 API로 다시 업로드하면 기존 이미지가 교체됩니다.

다른 부모의 `childProfileId`를 사용하면 `404 Not Found`가 반환됩니다.

### S3 환경변수

```env
AWS_REGION=ap-northeast-2
S3_BUCKET=버킷명
S3_PROFILE_IMAGE_PREFIX=children
```

EC2에서는 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`를 서버에 저장하지 않고 EC2 IAM 역할을 사용합니다. 역할에는 다음 권한이 필요합니다.

```text
s3:PutObject
s3:GetObject
s3:DeleteObject
```

## 7. 상담 상황 생성

로그인한 부모가 본인이 소유한 아이의 상담 상황을 생성합니다. 생성된 상담 세션 ID는 이후 녹음 및 WebSocket 연결에 사용합니다.

### Request

```http
POST /api/children/{childProfileId}/counseling-sessions
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "title": "학원 숙제 때문에 갈등이 생겼어요",
  "content": "오늘 아이가 숙제를 하지 않아 이야기하는 과정에서 서로 감정이 상했습니다."
}
```

### Response

상태 코드: `201 Created`

```json
{
  "message": "상담 상황 생성 성공",
  "data": {
    "id": 1,
    "date": "2026-08-19",
    "title": "학원 숙제 때문에 갈등이 생겼어요",
    "content": "오늘 아이가 숙제를 하지 않아 서로 감정이 상했습니다."
  }
}
```

`title`은 필수이며 최대 200자입니다. `content`도 필수입니다.

## 9. 아이별 상담 기록 목록 조회

상담 기록은 최신순으로 조회하며 무한 스크롤을 위해 `cursorId`를 사용합니다. 한 번에 최대 5개까지 조회합니다.
    "content": "오늘 아이가 숙제를 하지 않아 이야기하는 과정에서 서로 감정이 상했습니다."
  }
}
```

`title`은 필수이며 최대 200자입니다. `content`도 필수입니다. `date`는 생성일 기준 `yyyy-MM-dd` 형식입니다.

다른 부모의 `childProfileId`를 사용하면 `404 Not Found`가 반환됩니다.

## 8. 아이별 상담 기록 목록 조회

로그인한 부모가 본인이 소유한 아이와 진행한 상담 기록을 최신순으로 조회합니다. 한 번에 최대 5개까지 조회하며 `cursorId`를 사용해 다음 목록을 요청합니다.

### Request

```http
GET /api/children/{childProfileId}/counseling-sessions?cursorId=20&size=5
Authorization: Bearer {accessToken}
```

첫 목록은 `cursorId` 없이 요청합니다.

목록 항목도 생성 응답과 동일한 `CounselingSessionResponse`를 사용합니다. 각 항목의 `id`로 상담 세션을 식별할 수 있습니다.

### Response

상태 코드: `200 OK`

```json
{
  "message": "상담 기록 조회 성공",
  "data": {
    "items": [
      {
        "id": 1,
        "date": "2026-08-19",
        "title": "학원 숙제 때문에 갈등이 생겼어요",
        "content": "오늘 아이가 숙제를 하지 않아 서로 감정이 상했습니다."
        "content": "오늘 아이가 숙제를 하지 않아 이야기하는 과정에서 서로 감정이 상했습니다."
      }
    ],
    "nextCursorId": 15,
    "hasNext": true
  }
}
```

다음 목록이 있으면 `nextCursorId`를 다음 요청의 `cursorId`로 전달합니다. `size`는 1 이상 5 이하만 허용합니다.

## 10. 상담 세션 상세 조회

상담 세션의 현재 상태와 대화 내용, 분석 결과를 조회합니다. 실제 AI 분석이 완료되지 않은 세션은 `analysisReport`가 `null`입니다.

### Request

```http
GET /api/children/{childProfileId}/counseling-sessions/{sessionId}
Authorization: Bearer {accessToken}
```

### Response

상태 코드: `200 OK`

```json
{
  "message": "상담 세션 상세 조회 성공",
  "data": {
    "id": 1,
    "date": "2026-08-19",
    "title": "학원 숙제 때문에 갈등이 생겼어요",
    "content": "오늘 아이가 숙제를 하지 않아 서로 감정이 상했습니다.",
    "status": "COMPLETED",
    "startedAt": "2026-08-19T15:00:00",
    "endedAt": "2026-08-19T15:20:00",
    "conversation": {
      "turns": [
        {
          "role": "user",
          "text": "너무 어려워요"
        },
        {
          "role": "assistant",
          "text": "어떤 게 어려웠는지 같이 얘기해볼까?"
        }
      ],
      "text": "아이: 너무 어려워요\n에이전트: 어떤 게 어려웠는지 같이 얘기해볼까?"
    },
    "analysisReport": {
      "summary": "아이와 부모 모두 숙제 문제로 감정이 격해졌습니다.",
      "emotionSummary": "아이에게 부담감과 서운함이 관찰되었습니다.",
      "parentingGuidance": "지시보다 감정 확인을 먼저 시도해보세요.",
      "resultPayload": null,
      "modelName": "model-name",
      "promptVersion": "v1"
    }
  }
}
```

상담 상태는 `DRAFT`, `RECORDING`, `TRANSCRIBING`, `ANALYZING`, `COMPLETED`, `FAILED` 중 하나입니다. 다른 부모의 아이 또는 상담 세션이면 `404 Not Found`가 반환됩니다.

## 11. 녹음 시작 준비

녹음 버튼을 누를 때 호출합니다. 성공 후 응답의 `id`를 사용해 WebSocket에 연결합니다. WebSocket 연결 ID는 DB에 저장하지 않습니다.

### Request

```http
POST /api/children/{childProfileId}/counseling-sessions/{sessionId}/start
Authorization: Bearer {accessToken}
```

`DRAFT` 또는 `FAILED` 상태에서만 시작할 수 있습니다. 시작하면 상태가 `RECORDING`으로 바뀌고 `startedAt`이 기록됩니다.

`RECORDING`, `TRANSCRIBING`, `ANALYZING`, `COMPLETED` 상태에서 호출하면 `409 Conflict`가 반환됩니다.
다음 목록이 있으면 응답의 `nextCursorId`를 다음 요청의 `cursorId`로 전달합니다. `size`는 1 이상 5 이하만 허용합니다.

## 12. 아이 프로필 수정

로그인한 부모가 본인이 소유한 아이 프로필의 이름, 생년월일, 성별을 수정합니다. 기존 아이 프로필 생성 요청과 동일한 요청 형식을 사용합니다. 프로필 이미지는 기존 이미지 업로드 API에서 별도로 수정합니다.

### Request

```http
PUT /api/children/{childProfileId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "name": "김아이",
  "birthDate": "2015-03-20",
  "gender": "FEMALE"
}
```

### Response

상태 코드: `200 OK`

```json
{
  "message": "아이 프로필 수정 성공",
  "data": {
    "id": 1,
    "name": "김아이",
    "birthDate": "2015-03-20",
    "gender": "FEMALE",
    "profileImageUrl": null
  }
}
```

## 13. 아이 프로필 삭제

로그인한 부모가 본인이 소유한 아이 프로필을 삭제합니다. 아이에게 연결된 상담 세션, 대화 메시지, 분석 리포트, 녹음 메타데이터도 함께 삭제됩니다. 연결된 S3 프로필 이미지와 녹음 파일은 삭제를 시도하며, S3 삭제 실패는 로그로 기록됩니다.

### Request

```http
DELETE /api/children/{childProfileId}
Authorization: Bearer {accessToken}
```

### Response

상태 코드: `200 OK`

```json
{
  "message": "아이 프로필 삭제 성공",
  "data": null
}
```

## 14. 상담 세션 삭제

로그인한 부모가 본인이 소유한 상담 세션을 삭제합니다. 모든 상담 상태에서 삭제할 수 있으며, 연결된 대화 메시지, 분석 리포트, 녹음 메타데이터도 함께 삭제됩니다.

### Request

```http
DELETE /api/children/{childProfileId}/counseling-sessions/{sessionId}
Authorization: Bearer {accessToken}
```

`DRAFT`, `RECORDING`, `TRANSCRIBING`, `ANALYZING`, `COMPLETED`, `FAILED` 상태 모두 삭제할 수 있습니다. 진행 중인 세션도 즉시 삭제되므로, 프론트에서는 삭제 전에 진행 중인 음성 연결을 종료해야 합니다.

### Response

상태 코드: `200 OK`

```json
{
  "message": "상담 세션 삭제 성공",
  "data": null
}
```

## 15. 대화 JSON 저장

voice-client가 대화 종료 후 생성한 handoff JSON을 저장합니다. `turns`는 `ConversationMessage`로 저장되고, 저장된 대화는 상담 상세 조회에서 `conversation`으로 반환됩니다. 대화 저장이 커밋된 뒤 백엔드가 요약 서버를 동기 호출하며, 요약 서버가 같은 RDS의 `AnalysisReport`를 저장한 뒤 상담 세션을 `COMPLETED`로 변경합니다.

### Request

```http
POST /api/children/{childProfileId}/counseling-sessions/{sessionId}/handoff
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "turns": [
    {
      "role": "user",
      "text": "너무 어려워요",
      "itemId": "item_1",
      "status": "completed"
    },
    {
      "role": "assistant",
      "text": "어떤 게 어려웠는지 같이 얘기해볼까?",
      "itemId": "item_2",
      "status": "completed"
    }
  ],
  "text": "아이: 너무 어려워요\n에이전트: 어떤 게 어려웠는지 같이 얘기해볼까?"
}
```

`role`은 `user` 또는 `assistant`만 허용합니다. `itemId`와 `status`는 현재 DB에 저장하지 않습니다. `RECORDING` 상태의 세션만 대화를 저장할 수 있으며, 저장 후 상담 상세 조회에서 대화 내용을 확인할 수 있습니다.

### Response

상태 코드: `200 OK`

응답은 저장된 `conversation`과 요약 서버가 저장한 `analysisReport`가 포함된 상담 세션 상세 응답과 동일한 형식입니다. 요약 서버 오류 시 대화 원문은 보존되고 세션은 `FAILED` 상태가 되며 `502 Bad Gateway`가 반환됩니다.

## 16. Realtime 음성 통화 임시 키 발급

로그인한 부모가 브라우저 음성 통화를 시작할 때 호출합니다. 백엔드는 서버 환경변수의 OpenAI API 키로 Realtime 임시 키를 발급받은 뒤, 브라우저에는 짧은 수명의 `clientSecret`만 반환합니다.

### Request

```http
POST /api/voice/realtime-token
Authorization: Bearer {accessToken}
```

Request body는 없습니다. 부모 권한이 없는 요청은 거부됩니다.

### Response

```json
{
  "message": "Realtime 임시 키 발급 성공",
  "data": {
    "clientSecret": "ek_...",
    "expiresAt": 1760000000
  }
}
```

백엔드는 다음 환경변수만 사용합니다. 실제 값은 `.env`나 배포 환경에만 설정하고 저장소에는 커밋하지 않습니다.

```env
OPENAI_API_KEY=sk-...
OPENAI_REALTIME_CLIENT_SECRETS_URL=https://api.openai.com/v1/realtime/client_secrets
SUMMARY_SERVER_URL=http://54.180.117.230:8000
SUMMARY_CONNECT_TIMEOUT=5s
SUMMARY_READ_TIMEOUT=60s
```

`OPENAI_REALTIME_CLIENT_SECRETS_URL`은 기본값이 있으므로 일반적으로 생략할 수 있습니다. 프론트에는 `OPENAI_API_KEY` 또는 `VITE_OPENAI_API_KEY`를 설정하지 않습니다.

상담 handoff가 완료되면 백엔드는 다음 요청으로 요약 서버를 호출합니다.

```http
POST /summarize
Content-Type: application/json
```

```json
{
  "session_id": 123
}
```

요약 서버의 응답 `session_id`가 요청한 상담 세션 ID와 일치하고, 같은 RDS의 `analysis_reports`에 결과가 저장된 경우에만 상담 세션을 `COMPLETED`로 처리합니다.

## 에러 응답

에러도 `CommonResponse<Void>` 형식을 사용합니다.

```json
{
  "message": "loginId 또는 password가 올바르지 않습니다.",
  "data": null
}
```

| 상황 | 상태 코드 | 메시지 |
| --- | --- | --- |
| 필수값 누락 | `400 Bad Request` | 각 필드의 필수 입력 메시지 |
| 중복 loginId | `409 Conflict` | `이미 사용 중인 loginId입니다.` |
| 로그인 실패 | `401 Unauthorized` | `loginId 또는 password가 올바르지 않습니다.` |
| Refresh Token 오류 | `401 Unauthorized` | `Refresh Token이 유효하지 않거나 만료되었습니다.` |
| 잘못된 대화 role | `400 Bad Request` | `role은 user 또는 assistant만 사용할 수 있습니다.` |
| 아이 프로필을 찾을 수 없음 | `404 Not Found` | `아이 프로필을 찾을 수 없습니다.` |
| 상담 세션을 찾을 수 없음 | `404 Not Found` | `상담 세션을 찾을 수 없습니다.` |
| 녹음 시작 불가 상태 | `409 Conflict` | `현재 상담 상태에서는 녹음을 시작할 수 없습니다.` |
| 대화 저장 불가 상태 | `409 Conflict` | `RECORDING 상태의 상담 세션만 대화를 저장할 수 있습니다.` |
| 상담 요약 서버 오류 | `502 Bad Gateway` | `상담 요약 서버에 연결할 수 없습니다.` 또는 요약 응답 오류 메시지 |

## CORS 및 프론트 환경

로컬 프로필에서는 기본적으로 `http://localhost:5173`에서 오는 요청과 credentials를 허용합니다.

프론트의 API 기본 주소는 다음처럼 관리하는 것을 권장합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

프론트와 백엔드를 별도 EC2에 배포할 때는 백엔드 실행 환경변수에 프론트의 Origin을 주입합니다.

```bash
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGIN=https://front.example.com
```

`CORS_ALLOWED_ORIGIN`에는 경로를 제외한 프론트 주소를 넣습니다. 예를 들어
`https://front.example.com`은 가능하지만 `https://front.example.com/login`은 사용할 수 없습니다.

운영 프로필의 Refresh Token 쿠키는 기본적으로 `Secure=true`, `SameSite=None`입니다.
따라서 프론트·백엔드가 서로 다른 사이트에서 호출되면 HTTPS를 사용해야 합니다.
