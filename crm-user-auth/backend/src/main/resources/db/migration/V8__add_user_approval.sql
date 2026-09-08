-- V8: 用户注册审批流程
-- 为 sys_user 添加审批状态字段

ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';
COMMENT ON COLUMN sys_user.status IS '审批状态: PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝';

CREATE INDEX idx_sys_user_status ON sys_user(status) WHERE is_deleted = false;
