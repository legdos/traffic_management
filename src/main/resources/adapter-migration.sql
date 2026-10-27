-- Additive compatibility migration for transport_groupwork.
-- Run once after backup; do not re-import trans_groupwork_fixed.sql.
ALTER TABLE factory ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP(3) NULL;

ALTER TABLE need ADD COLUMN IF NOT EXISTS required_vehicle_type VARCHAR(80) NULL;
ALTER TABLE need ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PENDING';
ALTER TABLE need ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP(3) NULL;

ALTER TABLE poi ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP(3) NULL;

ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS license_number VARCHAR(32) NULL;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS current_action_code VARCHAR(40) NULL;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE';
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS route_id INT NULL;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS segment_index INT NULL;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS segment_progress DECIMAL(8,5) NOT NULL DEFAULT 0;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS position_at TIMESTAMP(3) NULL;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP(3) NULL;

-- Task assignment compatibility columns. These statements are additive and safe
-- for the supplied transport_groupwork schema.
ALTER TABLE trans_task ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'LEGACY';
ALTER TABLE trans_task ADD COLUMN IF NOT EXISTS match_score DECIMAL(8,5) NULL;
ALTER TABLE trans_task ADD COLUMN IF NOT EXISTS active_need_id INT NULL;
ALTER TABLE trans_task ADD COLUMN IF NOT EXISTS active_vehicle_id INT NULL;
ALTER TABLE trans_task ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP(3) NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_trans_task_active_need ON trans_task(active_need_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_trans_task_active_vehicle ON trans_task(active_vehicle_id);

CREATE TABLE IF NOT EXISTS action_definition (
  code INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  action_type VARCHAR(40) NOT NULL,
  default_duration_sec INT NOT NULL DEFAULT 60,
  accident_rate DECIMAL(8,6) NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
