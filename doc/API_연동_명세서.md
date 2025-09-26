# API 연동 명세서

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