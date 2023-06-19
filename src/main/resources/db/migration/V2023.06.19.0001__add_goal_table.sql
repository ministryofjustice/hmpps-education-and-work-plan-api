CREATE TABLE goal
(
    id           VARCHAR(36)  PRIMARY KEY,
    reference    VARCHAR(36)  NOT NULL,
    title        VARCHAR(512) NOT NULL,
    review_date  DATE         NOT NULL,
    category     VARCHAR(50)  NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    notes        TEXT,
    version      SMALLINT     NOT NULL
);

CREATE UNIQUE INDEX goal_reference_idx ON goal
(
 reference
);

