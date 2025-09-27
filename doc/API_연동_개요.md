# API 연동 개요

## 인증을 위한 HTTP 헤더 설정
| 헤더 명          | 헤더 값                                                       | 필수여부             | 비고              |
|-----------------|------------------------------------------------------------|---------------------|-----------------------|
| Authorization   | Bearer Base64(HmacSHA256({API Method}{API URI}, {ApiKey})) | O                   | API Key : 임이의 Hash 값 (생성 api 제공)                                                                                         |
| X-MUSINSA-ID | {ApiId}                                                    | O                   | 사용자 ID                                                                                                                   |
| X-MUSINSA-TIMESTAMP | {Timestamp}                                                | O                   | 15분 간 유효함 System.currentTimeMillis()                                                                                     |
| Itempotency-Key| {UniqueKey}                                                | X                   | - 가맹점이 유니크한 값을 생성하여 전달 (최대 200자) - 동일 요청 전달 시 409 Conflict +  이전 요청에 대한 응답을 재전달 - GET 요청에 추가하는 경우 무시됨                    |
|X-FINGERPAY-ENCRYPTION-MODE| ON                                                         | △                   |  일부 보안이 필요한 POST API에서 사용하는 헤더 - 적용 시 요청 및 응답 Body 전체를 JWE로 암호화 적용 (원본 문자열: JSON 형식) -적용 필수로 기재된 API 외에는 해당 헤더를 전달해도 무시됨 |

## 멱등키(Idempotent Key)
### 동작 흐름
2. 컨트롤러 메서드에 @Idempotent 어노테이션을 붙임
2. 클라이언트가 API 호출 시 Idempotency-Key 헤더를 포함
3. AOP Aspect(IdempotencyAspect)가 해당 어노테이션을 감지
4. Aspect에서 Idempotency-Key 헤더 추출
- 키가 없으면 400 에러 반환
- 이미 처리된 키면 409 에러 반환 (409 CONFLICT)
- 신규 키면 저장 후 비즈니스 비지니스 로직 진행
5. 정상 처리 시 응답 반환

### 적용 방법
1. 컨트롤러의 멱등성 보장 메서드에 @Idempotent 추가
2. 클라이언트는 요청마다 고유한 Idempotency-Key 헤더를 포함
3. 위 AOP, 서비스, 저장소 코드가 프로젝트에 포함되어 있어야 함

### 개선사항
1. 멱등키 유효시간 지정 : N일간 유지한 뒤 삭제 처리  
2. 적용 방법
- 멱등키 저장 시 생성일(createdAt)을 함께 저장
- 주기적으로(createdAt 기준) 만료된 키 삭제 (스케줄러, 배치 등 활용)
- 조회 시에도 만료된 키는 무시

---

## 포인트 API 명세서

### 1. 포인트 적립
회원에게 포인트를 적립합니다. 적립 금액, 만료일, 수기 지급 여부 등 다양한 조건을 지정할 수 있습니다. 
적립된 포인트는 추후 사용 및 취소가 가능합니다.

#### POST `/api/v1/points/accruals`

#### 요청(Request)
| 필드명    | 타입      | 필수 | 길이    | 설명                                                            |
|-----------|-----------|----|-------|---------------------------------------------------------------|
| userId    | String    | Y  | 1~32  | 사용자 ID                                                        |
| amount    | Long      | Y  | -     | 적립 금액 (1~100,000 포인트)                                         |
| expireDays| Integer   | N  | -     | 만료일수 (1~1824 일)                                               |
| manual    | Boolean   | N  | -     | 수기 지급 여부 (수기:Y, 수기아님:N(기본))                                   |
| sourceType| String    | Y  | 1~20  | 적립 발생 유형(ORDER-주문/결제, EVENT-이벤트, MANUAL-관리자 지급)               |
| sourceId  | String    | N  | 1~100 | 적립 발생 원천 ID(sourceType=ORDER-주문번호, EVENT-이벤트코드, MANUAL-관리자ID) |


#### 응답(Response)
| 필드명   | 타입     | 길이 | 설명         |
|----------|----------|----|------------|
| pointKey | String   | 50  | 포인트 적립 Key |
| userId   | String | Y    | 1~32  | 사용자 ID        |
| amount   | Long     | -  | 적립 금액      |
| expireAt | Date     | -  | 만료일        |

---

### 2. 포인트 적립취소
사용되지 않은 사용자의 포인트를 적립 취소합니다. 이미 일부가 사용된 경우 취소할 수 없습니다.

#### POST `/api/v1/points/accruals/cancel/{pointKey}`

#### 요청(Request)
| 필드명   | 타입   | 필수 | 길이   | 설명                |
|----------|--------|------|--------|---------------------|
| userId   | String | Y    | 1~32  | 사용자 ID        |
| pointKey   | String | Y    | 50  | 포인트 적립 Key        |
| reason   | String | N    | 최대 100자 | 취소 사유        |

#### 응답(Response)
| 필드명          | 타입   | 길이   | 설명      |
|--------------|--------|--------|---------|
| pointKey     | String   | 50  | 포인트 적립 Key |
| userId   | String | Y    | 1~32  | 사용자 ID        |
| amount       | Long   | -      | 취소된 금액  |


---

### 3. 포인트 사용
회원이 주문 시 포인트를 사용할 때 호출합니다. 
사용 시 주문번호와 함께 기록되며 수기 지급 포인트 및 만료일이 임박한 포인트부터 차감됩니다.


#### POST `/api/v1/points/usages`

#### 요청(Request)
| 필드명     | 타입   | 필수 | 길이      | 설명                |
|---------|--------|------|---------|---------------------|
| userId   | String | Y    | 1~32    | 사용자 ID        |
| orderNo | String | Y    | 최대 50   | 주문번호            |
| amount  | Long   | Y    | -       | 사용 금액           |


#### 응답(Response)
| 필드명   | 타입   | 길이   | 설명                |
| -------- | ------ | ------ | ------------------- |
| usageKey | String | 50     | 사용 식별값         |
| orderNo  | String | 최대 50| 주문번호            |
| amount   | Long   | -      | 사용 금액           |


---

### 4. 포인트 사용 취소
포인트 사용을 취소합니다. 만료 포인트 취소 시 신규 적립이 발생할 수 있습니다.

#### POST `/api/v1/points/usages/{usageKey}/cancel`

#### 요청(Request)
| 필드명   | 타입   | 필수 | 길이      | 설명                |
| -------- | ------ | ---- | --------- | ------------------- |
| amount   | Long   | Y    | -         | 취소 금액           |
| reason   | String | N    | 최대 100자| 취소 사유           |

#### 응답(Response)
| 필드명        | 타입   | 길이   | 설명                                        |
| ------------- | ------ | ------ | ------------------------------------------- |
| usageKey      | String | 50     | 사용 식별값                                 |
| canceledAmount| Long   | -      | 취소된 금액                                 |
| newPointKey   | String | 50     | 만료 포인트 취소 시 신규 적립된 pointKey(없으면 null) |

---

## 5. 포인트 내역 조회

### GET `/api/v1/points/history?memberId={memberId}`

#### 응답(Response)
#### 응답(Response)
| 필드명 | 타입  | 길이 | 설명                |
| ------ | ----- | ---- | ------------------- |
| items  | Array | -    | 포인트 내역 리스트  |

- items 필드 구조

| 필드명   | 타입     | 길이   | 설명                |
| -------- | -------- | ------ | ------------------- |
| type     | String   | 최대 10| ACCRUAL, USE, CANCEL|
| pointKey | String   | 50     | 적립/사용 식별값    |
| amount   | Long     | -      | 금액                |
| orderNo  | String   | 최대 50| 주문번호(사용시)    |
| manual   | Boolean  | -      | 수기 지급 여부      |
| createdAt| DateTime | -      | 생성일시            |
| expireAt | DateTime | -      | 만료일(적립시)

---

### 공통 사항
- 모든 금액 단위는 원(₩)입니다.
- 최대 적립/보유 한도, 만료일 등은 별도 설정값(환경변수, DB 등)으로 관리합니다.
- 수기 지급 여부, 만료일, 주문번호 등은 명확히 구분되어 응답에 포함됩니다.
- 예외 및 에러 응답은 표준 HTTP Status 및 에러코드/메시지로 제공합니다.
