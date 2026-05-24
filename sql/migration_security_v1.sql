-- Migration securite v1 : table _migrations + colonne password_hash.
-- Idempotent. Le clair (colonne password) reste en place : il sera
-- remplace par null au prochain login de chaque utilisateur (lazy hash).

CREATE TABLE IF NOT EXISTS _migrations (
	filename VARCHAR(190) PRIMARY KEY,
	applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	checksum VARCHAR(64)
);

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='users' AND column_name='password_hash');
SET @ddl = IF(@has_col=0, 'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER password', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_email');
SET @ddl2 = IF(@has_idx=0, 'CREATE INDEX idx_users_email ON users(email)', 'SELECT 1');
PREPARE stmt2 FROM @ddl2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SELECT 'Migration security_v1 appliquee.' AS status;
