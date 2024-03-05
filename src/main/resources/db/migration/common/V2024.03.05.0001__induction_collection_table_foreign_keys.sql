-- Add foreign key constraints to embedded collection tables (belonging to an Induction)
ALTER TABLE not_working_reasons ADD CONSTRAINT fk_work_on_release_not_working_reason FOREIGN KEY (work_on_release_id) REFERENCES work_on_release (id);

ALTER TABLE affecting_ability_to_work ADD CONSTRAINT fk_work_on_release_affect_ability_to_work FOREIGN KEY (work_on_release_id) REFERENCES work_on_release (id);

ALTER TABLE training_type ADD CONSTRAINT fk_previous_training_training_type FOREIGN KEY (training_id) REFERENCES previous_training (id);
