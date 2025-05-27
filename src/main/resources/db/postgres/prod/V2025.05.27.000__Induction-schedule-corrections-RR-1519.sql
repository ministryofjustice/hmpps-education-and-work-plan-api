update induction_schedule set schedule_status = 'COMPLETED', updated_at = now(), updated_by = 'STEPHENMCALLISTER_GEN' where prison_number = 'A1092FF';
INSERT INTO induction_schedule_history
    (id, reference, prison_number, deadline_date, schedule_calculation_rule, schedule_status, created_at, created_by, updated_at, updated_by, exemption_reason, version, created_at_prison, updated_at_prison)
VALUES ('aba0e1aa-9c88-4d26-a0e2-97d0b33f8abe', 'e55bf688-1b1c-4faa-8210-309ee057751b', 'A1092FF', '2025-10-01', 'EXISTING_PRISONER', 'COMPLETED', '2025-02-10 19:17:27.591 +00:00', 'system', now(), 'STEPHENMCALLISTER_GEN', null, 11, 'LEI', 'MDI');


update induction_schedule set schedule_status = 'COMPLETED', updated_at = now(), updated_by = 'STEPHENMCALLISTER_GEN' where prison_number = 'A5777EV';
INSERT INTO induction_schedule_history (id, reference, prison_number, deadline_date, schedule_calculation_rule, schedule_status, created_at, created_by, updated_at, updated_by, exemption_reason, version, created_at_prison, updated_at_prison)
VALUES ('8f743b51-f70b-4634-ae01-f38e3ab8f953', 'd9738980-6b17-41d0-bcff-05f089be9c13', 'A5777EV', '2025-10-01', 'EXISTING_PRISONER', 'COMPLETED', '2025-02-11 14:48:08.656 +00:00', 'system', now(), 'STEPHENMCALLISTER_GEN', null, 7, 'LEI', 'WEI');