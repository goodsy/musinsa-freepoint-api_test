-- API 호출 로그 테이블: 외부/내부 API 요청 및 응답 기록
CREATE TABLE IF NOT EXISTS api_log (
    log_id VARCHAR(50) PRIMARY KEY,                -- 로그 식별자
    api_method VARCHAR(10),                        -- HTTP 메서드(GET, POST 등)
    api_uri VARCHAR(128),                          -- 호출된 API URI
    idempotency_key VARCHAR(128),                  -- 멱등성 키(중복 방지용)
    request_headers VARCHAR(500),                  -- 요청 헤더
    request_body CLOB,                             -- 요청 본문
    response_body CLOB,                            -- 응답 본문
    status_code VARCHAR(5),                        -- 응답 상태 코드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 생성 시각
);

-- 포인트 정책 규칙 테이블: 사용자/글로벌 정책 관리
CREATE TABLE IF NOT EXISTS point_policy_rule (
   scope        VARCHAR(10)  NOT NULL,  -- 정책 범위(USER, GLOBAL)
   scope_id     VARCHAR(64)  NOT NULL,  -- 범위 식별자(사용자ID 또는 'ALL')
   policy_key   VARCHAR(50)  NOT NULL,  -- 정책 키(정책 종류)
   policy_value  BIGINT       DEFAULT 0 , -- 정책 값
   created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
   PRIMARY KEY (scope, scope_id, policy_key)
);
CREATE INDEX IF NOT EXISTS idx_policy_rule_key_scope_subject ON point_policy_rule (policy_key, scope, scope_id);

-- 포인트 지갑 요약 테이블: 사용자별 포인트 잔액 관리
CREATE TABLE IF NOT EXISTS point_wallet (
    user_id VARCHAR(10) PRIMARY KEY COMMENT '사용자 식별자 (지갑 소유자)',
    total_balance BIGINT NOT NULL DEFAULT 0 COMMENT '총 보유 포인트 잔액 (사용 가능 총합)',
    manual_balance BIGINT NOT NULL DEFAULT 0 COMMENT '관리자 수기 지급 포인트 잔액 (우선 사용 대상)',
    total_used BIGINT NOT NULL DEFAULT 0 COMMENT '총 사용 포인트 금액',
    total_canceled BIGINT NOT NULL DEFAULT 0 COMMENT '총 취소 포인트 금액',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 업데이트 시간'
) COMMENT='포인트 지갑 요약: 사용자별 전체/수기 포인트 잔액 스냅샷 관리';

-- 포인트 적립 원장 테이블: 포인트 적립 이벤트 상세 내역
CREATE TABLE IF NOT EXISTS point_accrual (
    point_key VARCHAR(50) PRIMARY KEY COMMENT '포인트 Key',
    user_id VARCHAR(10) NOT NULL COMMENT '사용자 식별자',
    amount BIGINT NOT NULL COMMENT '적립 금액',
    remain_amount BIGINT NOT NULL COMMENT '현재 남아있는 사용 가능 금액',
    manual BOOLEAN NOT NULL DEFAULT FALSE COMMENT '관리자 수기 지급 여부',
    source_type VARCHAR(20) NOT NULL COMMENT '적립 발생 유형',
    source_id VARCHAR(100) COMMENT '적립 발생 원천 ID',
    expires_at TIMESTAMP NOT NULL COMMENT '만료일시',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, CANCELED, EXPIRED)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  COMMENT '수정 시간'
) COMMENT='포인트 적립 원장: 사용자별 포인트 적립 이벤트 상세 내역 관리';
-- 사용 배분 정렬 최적화 인덱스
CREATE INDEX IF NOT EXISTS idx_accrual_use_pick ON point_accrual(user_id, status, remain_amount, manual DESC, expires_at ASC, created_at ASC);
-- 원천 추적/중복 방지 인덱스
CREATE INDEX IF NOT EXISTS idx_accrual_source_lookup ON point_accrual(user_id, source_type, source_id);

-- 포인트 사용 원장 테이블: 포인트 사용/취소 내역
CREATE TABLE IF NOT EXISTS point_usage (
    usage_key VARCHAR(50) PRIMARY KEY COMMENT '사용 내역 Key',
    user_id VARCHAR(10) NOT NULL,                                 -- 사용자 식별자
    order_no VARCHAR(100) NOT NULL COMMENT '주문번호',
    amount BIGINT NOT NULL COMMENT '사용/취소 금액',
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                  -- 사용 시각
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,               -- 생성 시각
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,               -- 수정 시각
    reversal_of_id BIGINT NULL COMMENT '사용 취소 시 원본 사용 내역 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'USE' COMMENT '사용/취소 구분 (USE: 사용, CANCEL: 사용취소)'
) COMMENT='포인트 사용 내역';
CREATE INDEX IF NOT EXISTS idx_usage_user_time ON point_usage(user_id, used_at DESC, usage_key DESC);

-- 포인트 사용 상세 테이블: 사용/취소 시 적립내역별 상세 내역
CREATE TABLE IF NOT EXISTS point_usage_detail (
    usage_detail_key VARCHAR(50) PRIMARY KEY COMMENT '사용 상세 내역 Key',
    usage_key BIGINT NOT NULL,                                   -- 사용 내역 Key
    point_key VARCHAR(50) NOT NULL COMMENT '사용 또는 취소 시 적립내역 point_key',
    amount BIGINT NOT NULL,                                      -- 사용 금액
    canceled_amount BIGINT NOT NULL DEFAULT 0 COMMENT '취소된 금액',
    expired BOOLEAN NOT NULL DEFAULT FALSE COMMENT '사용 취소 시점에 만료된 적립금 사용 여부',
    CONSTRAINT fk_usage_detail_accrual FOREIGN KEY (point_key) REFERENCES point_accrual(point_key)
) COMMENT='포인트 사용에 대한 상세 내역';
