--- Table timeline
CREATE TABLE timeline
(
    id            UUID  PRIMARY KEY,
    reference     UUID  NOT NULL,
    prison_number VARCHAR(10) NOT NULL,
    created_at    TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(3) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX timeline_prison_number_idx ON timeline
(
    prison_number
);

CREATE UNIQUE INDEX timeline_reference_idx ON timeline
(
     reference
);


--- Table timeline_event
CREATE TABLE timeline_event
(
    id                      UUID  PRIMARY KEY,
    reference               UUID  NOT NULL,
    timeline_id             UUID  NOT NULL,
    source_reference        VARCHAR(50)  NOT NULL,
    event_type              VARCHAR(50)  NOT NULL,
    contextual_info         VARCHAR(512) NOT NULL,
    created_by              VARCHAR(50)  NOT NULL,
    created_by_display_name VARCHAR(100) NOT NULL,
    timestamp               TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at_prison       VARCHAR(3)  NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_timeline_timeline_event FOREIGN KEY (timeline_id) REFERENCES timeline(id)
);

CREATE UNIQUE INDEX timeline_event_reference_idx ON timeline_event
(
    reference
);
CREATE INDEX timeline_event_timeline_id_idx ON timeline_event
(
    timeline_id
);
