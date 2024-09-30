-- table note
CREATE TABLE note
(
    id                 UUID         PRIMARY KEY,
    reference          UUID         NOT NULL,
    content            TEXT         NOT NULL,
    prison_number      VARCHAR(10)  NOT NULL,
    created_at         TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at_prison  VARCHAR(10)  NOT NULL,
    created_by         VARCHAR(50)  NOT NULL,
    updated_at         TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by         VARCHAR(50)  NOT NULL,
    updated_at_prison  VARCHAR(10)  NOT NULL,
    note_type          VARCHAR(10)  NOT NULL,
    entity_type        VARCHAR(50)  NOT NULL,
    entity_reference   UUID         NOT NULL
);

CREATE UNIQUE INDEX note_reference_idx ON note
    (
     reference
        );
CREATE INDEX prison_number_idx ON note
    (
     prison_number
        );

-- Migration script to create entries in the notes table from the goal table

INSERT INTO note (
    id,
    reference,
    content,
    prison_number,
    created_at,
    created_at_prison,
    created_by,
    updated_at,
    updated_by,
    updated_at_prison,
    note_type,
    entity_type,
    entity_reference
)
SELECT
    gen_random_uuid() AS id,
    gen_random_uuid() AS reference,
    g.notes AS content,
    ap.prison_number AS prison_number,
    g.created_at,
    g.created_at_prison,
    g.created_by,
    g.updated_at,
    g.updated_by,
    g.updated_at_prison,
    'GOAL' AS note_type,
    'GOAL' AS entity_type,
    g.reference AS entity_reference
FROM
    goal g
        JOIN
    action_plan ap ON g.action_plan_id = ap.id;

-- Will add another PR with the drop column once tested in dev
-- ALTER TABLE goal
--     DROP COLUMN notes;