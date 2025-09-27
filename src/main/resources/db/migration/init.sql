
CREATE TABLE IF NOT EXISTS point_policy_rule (
   scope        VARCHAR(10)  NOT NULL,  -- 'USER' |'GLOBAL'
   scope_id     VARCHAR(64)  NOT NULL,  -- scope=USER : userId, scope=GLOBAL: 'ALL'
   policy_key   VARCHAR(50)  NOT NULL,  -- 'accrual.minPerTxn' | 'accrual.maxPerTxn' | 'expiry.maxExpiryDays' | 'wallet.maxBalance'
   policy_value  BIGINT       DEFAULT 0 ,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
   updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (scope, scope_id)
);
-- 조회 최적화 인덱스 (policy_key 기준 탐색)
CREATE INDEX IF NOT EXISTS idx_policy_rule_key_scope_subject ON point_policy_rule (policy_key, scope, scope_id);


CREATE TABLE IF NOT EXISTS point_wallet (
    user_id VARCHAR(10) PRIMARY KEY COMMENT '사용자 식별자 (지갑 소유자)',
    total_balance BIGINT NOT NULL DEFAULT 0 COMMENT '총 보유 포인트 잔액 (만료/상태 무관, 사용 가능 총합)',
    manual_balance BIGINT NOT NULL DEFAULT 0 COMMENT '관리자 수기 지급 포인트 잔액 (우선 사용 대상)',
    created_at TIMESTAMP DEFAULT TIMESTAMP  COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT TIMESTAMP  COMMENT '마지막 업데이트 시간'
) COMMENT='포인트 지갑 요약: 사용자별 전체/수기 포인트 잔액 스냅샷 관리';


CREATE TABLE IF NOT EXISTS point_accrual (
    point_key VARCHAR(50) PRIMARY KEY COMMENT '포인트 Key(문자열, PK)',
    user_id VARCHAR(10) NOT NULL COMMENT '사용자 식별자',
    amount BIGINT NOT NULL COMMENT '적립 금액',
    remain_amount BIGINT NOT NULL COMMENT '현재 남아있는 사용 가능 금액',
    manual BOOLEAN NOT NULL DEFAULT FALSE COMMENT '관리자 수기 지급 여부',
    source_type VARCHAR(20) NOT NULL COMMENT '적립 발생 유형 (ORDER-주문/결제 포인트 적립, EVENT-프로모션/이벤트, MANUAL-관리자지급, REVERSAL-사용 취소 시 환급(신규 적립으로 재발행된 케이스) 등)',
    source_id VARCHAR(100) COMMENT '적립 발생 원천 ID (주문번호, 이벤트코드, 수기 지급 요청 ID 등) (ORDER → 주문번호,  EVENT → 이벤트코드, MANUAL → 관리자 ID, REVERSAL : 원래 사용 내역 ID)',
    expires_at TIMESTAMP NOT NULL COMMENT '만료일시',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, CANCELED, EXPIRED)',
    memo VARCHAR(100) COMMENT '메모',
    created_at TIMESTAMP DEFAULT TIMESTAMP  COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT TIMESTAMP  COMMENT '수정 시간'
) COMMENT='포인트 적립 원장: 사용자별 포인트 적립 이벤트 상세 내역 관리';

-- 사용 배분 정렬 최적화
CREATE INDEX IF NOT EXISTS idx_accrual_use_pick ON point_accrual(user_id, status, remain_amount, manual DESC, expires_at ASC, point_key ASC);
-- 원천 추적/중복 방지(검색)
CREATE INDEX IF NOT EXISTS idx_accrual_source_lookup ON point_accrual(user_id, source_type, source_id);

-- 포인트 사용 원장
CREATE TABLE IF NOT EXISTS point_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(10) NOT NULL,
    order_no VARCHAR(100) NOT NULL COMMENT '주문번호 (포인트 사용이 발생한 주문 식별자)',
    amount BIGINT NOT NULL COMMENT '사용/취소 금액',
    idempotency_key VARCHAR(128),
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reversal_of_id BIGINT NULL COMMENT '사용 취소 시 원본 사용 내역 ID (NULL이면 원본, 값이 있으면 취소 내역)',
    status VARCHAR(20) NOT NULL DEFAULT 'USE' COMMENT '사용/취소 구분 (USE: 사용, CANCEL: 사용취소)'
) COMMENT='포인트 사용 내역';
CREATE INDEX IF NOT EXISTS idx_usage_user_time ON point_usage(user_id, used_at DESC, id DESC);


-- 포인트 사용 상세
CREATE TABLE IF NOT EXISTS point_usage_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usage_id BIGINT NOT NULL,
    point_key VARCHAR(50) NOT NULL COMMENT '사용 또느 취소 시 적립내역 point_key',
    amount BIGINT NOT NULL,
    canceled_amount BIGINT NOT NULL DEFAULT 0 COMMENT '취소된 금액 (부분 취소 시 사용)',
    expired BOOLEAN NOT NULL DEFAULT FALSE COMMENT '사용 취소 시점에 만료된 적립금 사용 여부',
    CONSTRAINT fk_usage_detail_accrual FOREIGN KEY (point_key) REFERENCES point_accrual(point_key)
) COMMENT='포인트 사용에 대한 상세 내역';;

CREATE TABLE IF NOT EXISTS api_log (
    log_id VARCHAR(50) PRIMARY KEY,
    api_method VARCHAR(10),
    api_uri VARCHAR(128),
    idempotency_key VARCHAR(128),
    request_headers VARCHAR(500),
    request_body CLOB,
    response_body CLOB,
    status_code VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_api_log_idempotency UNIQUE (idempotency_key)
    );

-- ALTER TABLE api_log ADD CONSTRAINT uq_api_log_idempotency UNIQUE (idempotency_key);
