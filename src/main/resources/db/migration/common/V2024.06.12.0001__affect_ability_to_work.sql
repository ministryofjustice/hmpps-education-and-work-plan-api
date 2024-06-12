--- Merge the "Reasons Not To Work" and "Affect Ability To Work" tables by:
---   1. Inserting all records from `not_working_reasons` into `affecting_ability_to_work`
---   2. Set `work_on_release.affecting_work_other` from `work_on_release.not_hoping_other` where `work_on_release.hoping_to_work != YES`
---   3. Correcting some of the values based on the new enum values
---   4. Dropping the table `not_working_reasons`
---   5. Dropping the column `work_on_release.not_hoping_other`
---
--- `not_working_reasons` contains reasons for prisoners who do not want to work (`work_on_release.hoping_to_work != YES`)
--- and `affecting_ability_to_work` contains reasons for prisoners who want to work (`work_on_release.hoping_to_work != NO`).
--- Therefore the resultant table will not contain duplicates for any given prisoner's Induction.
--- Likewise `work_on_release.not_hoping_other` contains the OTHER reason for prisoners who do not want to work (`work_on_release.hoping_to_work != YES`)
--- and `work_on_release.affecting_work_other` contains the OTHER reason for prisoners who do want to work (`work_on_release.hoping_to_work = YES`)
---
--- The result is a single table `affecting_ability_to_work` that contains all the reasons for not wanting to work, plus
--- all the factors affecting ability to work.

--- 1. Insert all records from `not_working_reasons` into `affecting_ability_to_work` (where `work_on_release.hoping_to_work != YES`)
INSERT INTO affecting_ability_to_work (work_on_release_id, affect)
    SELECT nwr.work_on_release_id, nwr.reason AS affect
        FROM not_working_reasons AS nwr
        INNER JOIN work_on_release AS wor ON wor.id = nwr.work_on_release_id
        WHERE wor.hoping_to_work != 'YES';

--- 2. Set `work_on_release.affecting_work_other` from `work_on_release.not_hoping_other` for inductions where the prispner does not want to work (where `work_on_release.hoping_to_work != YES`)
UPDATE work_on_release
    SET affecting_work_other = not_hoping_other
    WHERE hoping_to_work != 'YES';

--- 3. Correct some of the values based on the new enum values
UPDATE affecting_ability_to_work
    SET affect = 'LIMITED_BY_OFFENCE'
    WHERE affect = 'LIMIT_THEIR_ABILITY' OR affect = 'LIMITED_BY_OFFENSE';
UPDATE affecting_ability_to_work
    SET affect = 'CARING_RESPONSIBILITIES'
    WHERE affect = 'FULL_TIME_CARER';
UPDATE affecting_ability_to_work
    SET affect = 'NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH'
    WHERE affect = 'HEALTH_ISSUES';
UPDATE affecting_ability_to_work
    SET affect = 'UNABLE_TO_WORK_DUE_TO_HEALTH'
    WHERE affect = 'HEALTH';
UPDATE affecting_ability_to_work
    SET affect = 'REFUSED_SUPPORT_WITH_NO_REASON'
    WHERE affect = 'NO_REASON';

--- 4. Drop the table `not_working_reasons`
DROP TABLE not_working_reasons;

--- 5. Drop the column `work_on_release.not_hoping_other`
ALTER TABLE work_on_release
    DROP COLUMN not_hoping_other;
