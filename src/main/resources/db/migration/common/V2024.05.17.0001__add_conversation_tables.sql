create table conversation_note
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    content                    TEXT,
    created_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by                 VARCHAR(50)                 NOT NULL,
    created_by_display_name    VARCHAR(100)                NOT NULL,
    created_at_prison          VARCHAR(20),
    updated_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by                 VARCHAR(50)                 NOT NULL,
    updated_by_display_name    VARCHAR(100)                NOT NULL,
    updated_at_prison          VARCHAR(20)
);
CREATE UNIQUE INDEX conversation_note_reference_idx ON conversation_note
(
    reference
);

create table conversation
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    prison_number              VARCHAR(10)                 NOT NULL,
    conversation_note_id       UUID                        NOT NULL,
    type                       VARCHAR(50)                 NOT NULL,
    created_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by                 VARCHAR(50)                 NOT NULL,
    updated_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by                 VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_conversation_conversation_note FOREIGN KEY (conversation_note_id) REFERENCES conversation_note(id)
);
CREATE UNIQUE INDEX conversation_prison_number_idx ON conversation
(
    prison_number
);
CREATE UNIQUE INDEX conversation_reference_idx ON conversation
(
    reference
);
