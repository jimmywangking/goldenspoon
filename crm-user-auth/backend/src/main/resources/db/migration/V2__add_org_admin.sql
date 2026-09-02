ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS is_org_admin BOOLEAN NOT NULL DEFAULT false;

UPDATE sys_user SET is_org_admin = true WHERE role = 'ORG_ADMIN';
