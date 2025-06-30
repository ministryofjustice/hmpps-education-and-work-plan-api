create unique index review_schedule_one_scheduled_per_prison_number_idx
    on review_schedule (prison_number)
    where schedule_status = 'SCHEDULED';
