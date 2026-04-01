--- Alter the employability_skill column types to better reflect their enum content
--- Correct session_type values to not be ordinal indexes

alter table employability_skill
    alter column rating_code type varchar(255),
    alter column session_type type varchar(255);

update employability_skill
    set session_type = 'CIAG_INDUCTION' where session_type = '0';
update employability_skill
    set session_type = 'CIAG_REVIEW' where session_type = '1';
update employability_skill
    set session_type = 'EDUCATION_REVIEW' where session_type = '2';
update employability_skill
    set session_type = 'INDUSTRIES_REVIEW' where session_type = '3';
