--- Add the exemption_reason column to induction schedule

ALTER TABLE induction_schedule
ADD COLUMN exemption_reason VARCHAR(512);


