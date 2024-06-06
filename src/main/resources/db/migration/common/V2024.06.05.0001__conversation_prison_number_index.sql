DROP INDEX conversation_prison_number_idx;

CREATE INDEX conversation_prison_number_idx ON conversation
(
    prison_number
);
