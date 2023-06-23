--- Drop all the tables because the ID columns were originally created incorrectly
--- Given that there is no data in the tables the safest and easiest thing to do is to drop and re-create them
drop table step;
drop table goal;
drop table action_plan;

--- Table action_plan
CREATE TABLE action_plan
(
    id            UUID         PRIMARY KEY,
    prison_number VARCHAR(10)  NOT NULL,
    created_at    TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by    VARCHAR(50)  NOT NULL,
    updated_at    TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by    VARCHAR(50)  NOT NULL
);

CREATE UNIQUE INDEX action_plan_prison_number_idx ON action_plan
(
    prison_number
);


--- Table goal
CREATE TABLE goal
(
    id              UUID         PRIMARY KEY,
    action_plan_id  UUID         NOT NULL,
    reference       UUID         NOT NULL,
    title           VARCHAR(512) NOT NULL,
    review_date     DATE         NOT NULL,
    category        VARCHAR(50)  NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(50)  NOT NULL,
    updated_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by      VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_action_plan_goal FOREIGN KEY (action_plan_id) REFERENCES action_plan(id)
);

CREATE UNIQUE INDEX goal_reference_idx ON goal
(
    reference
);
CREATE INDEX goal_action_plan_id_idx ON goal
(
    action_plan_id
);


--- Table step
CREATE TABLE step
(
    id              UUID         PRIMARY KEY,
    goal_id         UUID         NOT NULL,
    reference       UUID         NOT NULL,
    title           VARCHAR(512) NOT NULL,
    target_date     DATE         NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    sequence_number SMALLINT     NOT NULL,
    created_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(50)  NOT NULL,
    updated_at      TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by      VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_goal_step FOREIGN KEY (goal_id) REFERENCES goal(id)
);

CREATE UNIQUE INDEX step_reference_idx ON step
(
    reference
);
CREATE INDEX step_goal_id_idx ON step
(
    goal_id
);
