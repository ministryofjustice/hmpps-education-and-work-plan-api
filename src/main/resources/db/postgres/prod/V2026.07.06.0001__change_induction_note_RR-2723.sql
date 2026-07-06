--- RR-2723 - Induction note added in error for prisoner A2358FL. Change the note content and corresponding timeline event data.
--- Induction completed at Pentonville (HMP) on 2026-06-23

update note
    set content = 'Induction note added in error'
    where id = '0325cd64-eb69-431a-bf75-02b0d2f9a6ce'
      and prison_number = 'A2358FL';

update timeline_event_contextual_info
    set value = 'Induction note added in error'
    where timeline_event_id = '88fc0f3a-ada4-4c83-a4c5-85e865d94e64'
      and name = 'COMPLETED_INDUCTION_NOTES';
