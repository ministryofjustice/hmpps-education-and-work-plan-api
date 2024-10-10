--- Remove incorrectly migrated note records where the content is empty
--- Add a constraint to prevent note records being created with empty content.

DELETE FROM note
WHERE content IS NULL
   OR TRIM(content) = '';

ALTER TABLE note
    ADD CONSTRAINT content_not_empty CHECK (TRIM(content) <> '');