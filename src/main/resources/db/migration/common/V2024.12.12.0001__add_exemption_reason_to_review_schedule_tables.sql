--- Add the exemption_reason column to review schedule and review schedule history tables

ALTER TABLE review_schedule
ADD COLUMN exemption_reason VARCHAR(512);

ALTER TABLE review_schedule_history
ADD COLUMN exemption_reason VARCHAR(512);
