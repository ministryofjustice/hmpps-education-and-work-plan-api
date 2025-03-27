--- RR-1318 increase column widths for goal & step titles, and induction & review exemption reasons.
--- This is because the UI validates at (for example) 512 characters, but a line break in the UI (single character)
--- accounts for 2 characters in the database (\r\n). Therefore the database columns need to be wider than the UI
--- validation to account for any line breaks the user has entered in their content.
--- Setting the column type to TEXT allows for the UI to change/increase its field length requirements in the future.
---
--- Strictly speaking this would apply to any text field submitted by the UI, but most are single line UI fields so
--- line breaks cannot be entered. The only multi-line text fields in the UI are goal and step titles, notes, and
--- induction & review exemption reasons, though the various 'notes' columns are already of type TEXT so do not need
--- changing.

ALTER TABLE GOAL
    ALTER COLUMN title TYPE TEXT;
ALTER TABLE STEP
    ALTER COLUMN title TYPE TEXT;

ALTER TABLE INDUCTION_SCHEDULE
    ALTER COLUMN exemption_reason TYPE TEXT;
ALTER TABLE INDUCTION_SCHEDULE_HISTORY
    ALTER COLUMN exemption_reason TYPE TEXT;

ALTER TABLE REVIEW_SCHEDULE
    ALTER COLUMN exemption_reason TYPE TEXT;
ALTER TABLE REVIEW_SCHEDULE_HISTORY
    ALTER COLUMN exemption_reason TYPE TEXT;
