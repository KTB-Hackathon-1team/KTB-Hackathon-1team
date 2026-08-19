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

## CORS 및 프론트 환경

현재 백엔드는 `http://localhost:5173`에서 오는 요청과 credentials를 허용합니다.

프론트의 API 기본 주소는 다음처럼 관리하는 것을 권장합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

프론트 배포 후에는 백엔드의 `application.yml`에서 `cors-allowed-origin`을 실제 프론트 주소로 변경해야 합니다.
