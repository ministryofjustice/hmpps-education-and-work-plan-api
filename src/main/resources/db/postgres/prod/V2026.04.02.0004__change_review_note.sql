--- RR-2144 - Review added in error for this person. Change the note content and corresponding timeline event data as refers to the wrong person
--- Service Now incident INC4298818
update note
    set content = 'Review added in error'
    where id = '64ff7a9b-58bc-48ec-ba31-9c2c178d4be2'
      and prison_number = 'A3559ED';

update timeline_event_contextual_info
    set value = 'Review added in error'
    where timeline_event_id = 'f6ff60b5-b8bc-4895-a83e-a90d37f8ff20'
      and name = 'COMPLETED_REVIEW_NOTES';
