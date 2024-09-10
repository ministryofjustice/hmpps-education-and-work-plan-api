--- Data fix for previous_qualifications.education_level to set any null values to `NOT_SURE`
--- as we no longer support null values for this question on screen.
--- At this time there is only 1 production record that this affects, that of prisoner `A7237EE`

UPDATE previous_qualifications
  SET education_level = 'NOT_SURE'
  WHERE education_level IS NULL;

