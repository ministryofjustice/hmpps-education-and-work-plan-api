CREATE TABLE goal
(
    id          VARCHAR(36) PRIMARY KEY,
    reference   VARCHAR(36)  NOT NULL,
    title       VARCHAR(512) NOT NULL,
    review_date DATE         NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    notes       TEXT,
    created_at  TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(3) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX goal_reference_idx ON goal
(
    reference
);


CREATE TABLE step
(
    id              VARCHAR(36) PRIMARY KEY,
    goal_id         VARCHAR(36)  NOT NULL,
    reference       VARCHAR(36)  NOT NULL,
    title           VARCHAR(512) NOT NULL,
    target_date     DATE         NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    sequence_number SMALLINT     NOT NULL,
    created_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_goalSteps FOREIGN KEY (goal_id) REFERENCES goal(id)
);

CREATE UNIQUE INDEX step_reference_idx ON step
(
    reference
);
CREATE UNIQUE INDEX step_goal_id_idx ON step
(
    goal_id
);
