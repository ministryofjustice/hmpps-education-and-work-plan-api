--- RR-2537 - Review added in error for prisoner A4667FK. Change the note content and corresponding timeline event data.
--- Review conducted by Merna Gali on 15 April 2026, marked as completed at Brixton (HMP) on 16 April 2026.
--- Service Now incident INC4356574

update note
    set content = 'Review added in error'
    where id = '37096e59-cb63-40d3-91b9-f688322dd733'
      and prison_number = 'A4667FK';

update timeline_event_contextual_info
    set value = 'Review added in error'
    where timeline_event_id = '7c7421c8-2cc0-487b-b9ea-0e15a797ed6f'
      and name = 'COMPLETED_REVIEW_NOTES';
