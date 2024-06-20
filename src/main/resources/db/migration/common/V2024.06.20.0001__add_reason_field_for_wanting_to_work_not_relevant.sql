--- Add column to record the reason why the prisoner wanting to work or not is Not Relevant
ALTER TABLE previous_work_experiences
    ADD COLUMN has_worked_before_not_relevant_reason VARCHAR(512)
