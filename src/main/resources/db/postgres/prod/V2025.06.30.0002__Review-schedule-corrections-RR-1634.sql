-- script to correct review schedules for prisoners who have had two SCHEDULED review schedules
-- created. This is done by removing the earliest schedule and history for it.

--A6157FG
delete from review_schedule where reference = '3b33e341-c77a-4826-90ea-5d158fb047fe';
delete from review_schedule_history where reference = '3b33e341-c77a-4826-90ea-5d158fb047fe';

--A1759EP
delete from review_schedule where reference = '25441500-45e8-4a6c-8baa-1330ee13981d';
delete from review_schedule_history where reference = '25441500-45e8-4a6c-8baa-1330ee13981d';

--A1320AR
delete from review_schedule where reference in ('82b86d51-a264-4638-ba4f-c036589aad48',
                                                'df71e610-38f7-47a5-b632-6fba08a71220',
                                                '3c99e5ca-5c00-425b-a7d1-3015c1f734c9');
delete from review_schedule_history where reference in ('82b86d51-a264-4638-ba4f-c036589aad48',
                                                    'df71e610-38f7-47a5-b632-6fba08a71220',
                                                     '3c99e5ca-5c00-425b-a7d1-3015c1f734c9');


--A8573DX
delete from review_schedule where reference = '13d9ce0d-d8bc-4e4c-b43a-b682bf5232cd';
delete from review_schedule_history where reference = '13d9ce0d-d8bc-4e4c-b43a-b682bf5232cd';

--A7550FF
delete from review_schedule where reference = 'ab1236b0-6256-4768-9401-8639567cb997';
delete from review_schedule_history where reference = 'ab1236b0-6256-4768-9401-8639567cb997';

