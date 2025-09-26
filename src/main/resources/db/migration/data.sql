
-- 포인트 적립 정책
INSERT INTO point_policy_rule VALUES ('GLOBAL','ALL','accrual.minPerTxn','1',CURRENT_TIMESTAMP);
INSERT INTO point_policy_rule VALUES ('GLOBAL','ALL','accrual.maxPerTxn','100000',CURRENT_TIMESTAMP);
INSERT INTO point_policy_rule VALUES ('GLOBAL','ALL','expiry.maxExpiryDays','1824',CURRENT_TIMESTAMP);
INSERT INTO point_policy_rule VALUES ('GLOBAL','ALL','wallet.maxBalance','10000000',CURRENT_TIMESTAMP);
INSERT INTO point_policy_rule VALUES ('USER','musinsaId','wallet.maxBalance','2000000',CURRENT_TIMESTAMP);