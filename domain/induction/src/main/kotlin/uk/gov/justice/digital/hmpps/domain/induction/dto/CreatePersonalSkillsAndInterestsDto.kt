package uk.gov.justice.digital.hmpps.domain.induction.dto

import uk.gov.justice.digital.hmpps.domain.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.induction.PersonalSkill

data class CreatePersonalSkillsAndInterestsDto(
  val skills: List<PersonalSkill>,
  val interests: List<PersonalInterest>,
  val prisonId: String,
)
