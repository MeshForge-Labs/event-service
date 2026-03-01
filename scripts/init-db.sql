-- Event Service – DB bootstrap (idempotent).
-- Hibernate (ddl-auto=update) creates tables automatically.
-- This script only ensures the schema exists.

-- events table is created/managed by Hibernate.
-- Nothing else required for a fresh event_db.
