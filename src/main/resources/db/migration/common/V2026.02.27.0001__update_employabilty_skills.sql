ALTER TABLE employability_skill
    ADD COLUMN IF NOT EXISTS session_type TEXT,
    ADD COLUMN IF NOT EXISTS session_type_description TEXT;

ALTER TABLE employability_skill
DROP COLUMN IF EXISTS activity_name,
  DROP COLUMN IF EXISTS conversation_date;

