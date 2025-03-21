UPDATE induction_schedule
SET schedule_calculation_rule = 'EXISTING_PRISONER',
    deadline_date = '2025-10-01'
WHERE schedule_calculation_rule = 'NEW_PRISON_ADMISSION'
  AND created_at < TIMESTAMP '2025-04-01 00:00:00';

UPDATE induction_schedule_history
SET schedule_calculation_rule = 'EXISTING_PRISONER',
    deadline_date = '2025-10-01'
WHERE schedule_calculation_rule = 'NEW_PRISON_ADMISSION'
  AND created_at < TIMESTAMP '2025-04-01 00:00:00';

UPDATE timeline_event_contextual_info
SET value = '2025-10-01'
WHERE value = '2025-04-21'
  AND name IN ('INDUCTION_SCHEDULE_DEADLINE_DATE', 'INDUCTION_SCHEDULE_DEADLINE_OLD');