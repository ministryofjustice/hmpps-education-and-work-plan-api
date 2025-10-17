ALTER TABLE in_prison_training_interest ALTER COLUMN training_type_other TYPE TEXT;
ALTER TABLE in_prison_work_interest ALTER COLUMN work_type_other TYPE TEXT;
ALTER TABLE personal_interest ALTER COLUMN interest_type_other TYPE TEXT;
ALTER TABLE personal_skill ALTER COLUMN skill_type_other TYPE TEXT;
ALTER TABLE work_interest ALTER COLUMN work_type_other TYPE TEXT;
ALTER TABLE goal ALTER COLUMN archive_reason_other TYPE TEXT;
ALTER TABLE previous_training ALTER COLUMN training_type_other TYPE TEXT;
ALTER TABLE previous_work_experiences ALTER COLUMN has_worked_before_not_relevant_reason TYPE TEXT;
ALTER TABLE timeline_event_contextual_info ALTER COLUMN "name" TYPE TEXT;
ALTER TABLE work_experience ALTER COLUMN details TYPE TEXT;
ALTER TABLE work_interest ALTER COLUMN "role" TYPE TEXT;
ALTER TABLE work_on_release ALTER COLUMN affecting_work_other TYPE TEXT;
ALTER TABLE work_experience ALTER COLUMN experience_type_other TYPE TEXT;
ALTER TABLE work_experience ALTER COLUMN "role" TYPE TEXT;


