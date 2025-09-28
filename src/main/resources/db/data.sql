
-- 포인트 적립 정책
INSERT INTO point_policy_rule (scope, scope_id, policy_key, policy_value)
VALUES ('GLOBAL','ALL','accrual.minPerTxn', 1000);

INSERT INTO point_policy_rule (scope, scope_id, policy_key, policy_value)
VALUES ('GLOBAL','ALL','accrual.maxPerTxn', 50000);

INSERT INTO point_policy_rule (scope, scope_id, policy_key, policy_value)
VALUES ('GLOBAL','ALL','expiry.maxExpiryDays', 10);

INSERT INTO point_policy_rule (scope, scope_id, policy_key, policy_value)
VALUES ('GLOBAL','ALL','wallet.maxBalance', 60000);

INSERT INTO point_policy_rule (scope, scope_id, policy_key, policy_value)
VALUES ('USER','musinsaId','wallet.maxBalance', 10000);