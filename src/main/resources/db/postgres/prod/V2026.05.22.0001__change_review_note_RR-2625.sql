--- RR-2625 - Review added in error for prisoner A8155FG. Change the note content and corresponding timeline event data.
--- Review completed at Brixton (HMP) on 14 May 2026.
--- Service Now incident INC4455887

update note
    set content = 'Review added in error'
    where id = '88569f45-af41-4fda-8f0a-22bc0d4be296'
      and prison_number = 'A8155FG';

update timeline_event_contextual_info
    set value = 'Review added in error'
    where timeline_event_id = '3fc178e3-69c3-4594-832d-4010c4391637'
      and name = 'COMPLETED_REVIEW_NOTES';
