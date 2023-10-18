--- Table induction
CREATE TABLE induction
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    prison_number              VARCHAR(10)                 NOT NULL,
    work_on_release_id         UUID                        NOT NULL,
    previous_qualifications_id UUID,
    previous_training_id       UUID,
    work_experiences_id        UUID,
    in_prison_interests_id     UUID,
    skills_and_interests_id    UUID,
    future_work_interests_id   UUID,
    created_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by                 VARCHAR(50)                 NOT NULL,
    created_by_display_name    VARCHAR(100)                NOT NULL,
    created_at_prison          VARCHAR(20),
    updated_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by                 VARCHAR(50)                 NOT NULL,
    updated_by_display_name    VARCHAR(100)                NOT NULL,
    updated_at_prison          VARCHAR(20)
);

CREATE UNIQUE INDEX induction_prison_number_idx ON induction
(
    prison_number
);

--- Table previous_qualifications
CREATE TABLE previous_qualifications
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    education_level         VARCHAR(50)                 NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX previous_qualifications_reference_idx ON previous_qualifications
(
    reference
);

--- Table qualification
CREATE TABLE qualification
(
    id                     UUID PRIMARY KEY,
    prev_qualifications_id UUID                        NOT NULL,
    reference              UUID                        NOT NULL,
    subject                VARCHAR(100)                NOT NULL,
    level                  VARCHAR(50),
    grade                  VARCHAR(50),
    created_at             TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by             VARCHAR(50)                 NOT NULL,
    updated_at             TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by             VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_prev_qualifications_qualification FOREIGN KEY (prev_qualifications_id) REFERENCES previous_qualifications (id)
);

CREATE UNIQUE INDEX qualification_reference_idx ON qualification
(
     reference
);
CREATE INDEX previous_qualifications_qualification_id_idx ON qualification
(
     prev_qualifications_id
);


--- Table work_on_release
CREATE TABLE work_on_release
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    hoping_to_work          VARCHAR(50)                 NOT NULL,
    not_hoping_other        VARCHAR(512),
    affecting_work_other    VARCHAR(512),
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL

);

CREATE UNIQUE INDEX work_on_release_reference_idx ON work_on_release
(
     reference
);

-- Collection Table not_working_reasons
CREATE TABLE not_working_reasons
(
    work_on_release_id UUID NOT NULL,
    reason VARCHAR(50) NOT NULL
);

-- Collection Table affecting_ability_to_work
CREATE TABLE affecting_ability_to_work
(
    work_on_release_id UUID NOT NULL,
    affect VARCHAR(50) NOT NULL
);

--- Table training
CREATE TABLE previous_training
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    training_type_other     VARCHAR(512),
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX previous_training_reference_idx ON previous_training
(
     reference
);

-- Collection Table training_type
CREATE TABLE training_type
(
    training_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL
);


--- Table work_experiences
CREATE TABLE previous_work_experiences
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX previous_work_experiences_reference_idx ON previous_work_experiences
(
     reference
);

--- Table work_experience
CREATE TABLE work_experience
(
    id                    UUID PRIMARY KEY,
    work_experiences_id   UUID                        NOT NULL,
    reference             UUID                        NOT NULL,
    experience_type       VARCHAR(50)                 NOT NULL,
    experience_type_other VARCHAR(256),
    role                  VARCHAR(256),
    details               VARCHAR(512),
    created_at            TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by            VARCHAR(50)                 NOT NULL,
    updated_at            TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by            VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_previous_work_experiences_work_experience FOREIGN KEY (work_experiences_id) REFERENCES previous_work_experiences (id)
);

--- Table in_prison_interests
CREATE TABLE in_prison_interests
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX in_prison_interests_reference_idx ON in_prison_interests
(
     reference
);

--- Table in_prison_work_interest
CREATE TABLE in_prison_work_interest
(
    id                      UUID PRIMARY KEY,
    interests_id            UUID                        NOT NULL,
    reference               UUID                        NOT NULL,
    work_type               VARCHAR(50)                 NOT NULL,
    work_type_other         VARCHAR(255),
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_in_prison_interests_work_interest FOREIGN KEY (interests_id) REFERENCES in_prison_interests (id)
);

CREATE UNIQUE INDEX in_prison_work_interest_reference_idx ON in_prison_work_interest
(
     reference
);
CREATE INDEX work_interest_in_prison_interests_id_idx ON in_prison_work_interest
(
     interests_id
);

--- Table in_prison_training_interest
CREATE TABLE in_prison_training_interest
(
    id                      UUID PRIMARY KEY,
    interests_id            UUID                        NOT NULL,
    reference               UUID                        NOT NULL,
    training_type           VARCHAR(50)                 NOT NULL,
    training_type_other     VARCHAR(255),
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_in_prison_interests_training_interest FOREIGN KEY (interests_id) REFERENCES in_prison_interests (id)
);

CREATE UNIQUE INDEX in_prison_training_interest_reference_idx ON in_prison_training_interest
(
     reference
);
CREATE INDEX training_interest_in_prison_interests_id_idx ON in_prison_training_interest
(
     interests_id
);

--- Table skills_and_interests
CREATE TABLE skills_and_interests
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX skills_and_interests_reference_idx ON skills_and_interests
(
     reference
);

--- Table personal_skill
CREATE TABLE personal_skill
(
    id                  UUID PRIMARY KEY,
    skills_interests_id UUID                        NOT NULL,
    reference           UUID                        NOT NULL,
    skill_type          VARCHAR(50)                 NOT NULL,
    skill_type_other    VARCHAR(255),
    created_at          TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by          VARCHAR(50)                 NOT NULL,
    updated_at          TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by          VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_skills_and_interests_personal_skill FOREIGN KEY (skills_interests_id) REFERENCES skills_and_interests (id)
);

CREATE UNIQUE INDEX personal_skill_reference_idx ON personal_skill
(
     reference
);
CREATE INDEX personal_skill_skills_interests_id_idx ON personal_skill
(
     skills_interests_id
);

--- Table personal_interest
CREATE TABLE personal_interest
(
    id                  UUID PRIMARY KEY,
    skills_interests_id UUID                        NOT NULL,
    reference           UUID                        NOT NULL,
    interest_type       VARCHAR(50)                 NOT NULL,
    interest_type_other VARCHAR(255),
    created_at          TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by          VARCHAR(50)                 NOT NULL,
    updated_at          TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by          VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_skills_and_interests_personal_interest FOREIGN KEY (skills_interests_id) REFERENCES skills_and_interests (id)
);

CREATE UNIQUE INDEX personal_interest_reference_idx ON personal_interest
(
     reference
);
CREATE INDEX personal_interest_skills_interests_id_idx ON personal_interest
(
     skills_interests_id
);


--- Table work_interests
CREATE TABLE future_work_interests
(
    id                      UUID PRIMARY KEY,
    reference               UUID                        NOT NULL,
    created_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(50)                 NOT NULL,
    updated_at              TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by              VARCHAR(50)                 NOT NULL
);

CREATE UNIQUE INDEX future_work_interests_reference_idx ON future_work_interests
(
     reference
);

--- Table work_interest
CREATE TABLE work_interest
(
    id                UUID PRIMARY KEY,
    work_interests_id UUID                        NOT NULL,
    reference         UUID                        NOT NULL,
    work_type         VARCHAR(50)                 NOT NULL,
    work_type_other   VARCHAR(255),
    role              VARCHAR(512),
    created_at        TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by        VARCHAR(50)                 NOT NULL,
    updated_at        TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by        VARCHAR(50)                 NOT NULL,

    CONSTRAINT fk_future_work_interests_work_interest FOREIGN KEY (work_interests_id) REFERENCES future_work_interests (id)
);

CREATE UNIQUE INDEX work_interest_reference_idx ON work_interest
(
     reference
);
CREATE INDEX work_interest_work_interests_id_idx ON work_interest
(
     work_interests_id
);

ALTER TABLE induction ADD CONSTRAINT fk_work_on_release_induction FOREIGN KEY (work_on_release_id) REFERENCES work_on_release (id);
ALTER TABLE induction ADD CONSTRAINT fk_previous_qualifications_induction FOREIGN KEY (previous_qualifications_id) REFERENCES previous_qualifications (id);
ALTER TABLE induction ADD CONSTRAINT fk_previous_training_induction FOREIGN KEY (previous_training_id) REFERENCES previous_training (id);
ALTER TABLE induction ADD CONSTRAINT fk_previous_work_experiences_induction FOREIGN KEY (work_experiences_id) REFERENCES previous_work_experiences (id);
ALTER TABLE induction ADD CONSTRAINT fk_in_prison_interests_induction FOREIGN KEY (in_prison_interests_id) REFERENCES in_prison_interests (id);
ALTER TABLE induction ADD CONSTRAINT fk_skills_and_interests_induction FOREIGN KEY (skills_and_interests_id) REFERENCES skills_and_interests (id);
ALTER TABLE induction ADD CONSTRAINT fk_future_work_interests_induction FOREIGN KEY (future_work_interests_id) REFERENCES future_work_interests (id);

CREATE INDEX induction_work_on_release_id_idx ON induction
(
     work_on_release_id
);
CREATE INDEX induction_previous_qualifications_induction_id_idx ON induction
(
     previous_qualifications_id
);
CREATE INDEX induction_previous_training_induction_id_idx ON induction
(
     previous_training_id
);
CREATE INDEX induction_previous_work_experiences_induction_id_idx ON induction
(
     work_experiences_id
);
CREATE INDEX induction_in_prison_interests_induction_id_idx ON induction
(
     in_prison_interests_id
);
CREATE INDEX induction_skills_and_interests_induction_id_idx ON induction
(
     skills_and_interests_id
);
CREATE INDEX induction_future_work_interests_induction_id_idx ON induction
(
     future_work_interests_id
);
