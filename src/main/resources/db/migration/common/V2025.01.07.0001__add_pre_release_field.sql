UPDATE induction_schedule SET schedule_status = 'COMPLETED' where schedule_status = 'COMPLETE';
UPDATE induction_schedule_history SET schedule_status = 'COMPLETED' where schedule_status = 'COMPLETE';
