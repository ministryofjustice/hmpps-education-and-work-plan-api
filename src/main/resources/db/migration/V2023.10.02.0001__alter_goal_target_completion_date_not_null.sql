--- Alter goal table to make target_completion_date not nullable
UPDATE goal SET target_completion_date = '2023-12-31' WHERE target_completion_date IS NULL;
ALTER TABLE goal
    ALTER COLUMN target_completion_date SET NOT NULL;
