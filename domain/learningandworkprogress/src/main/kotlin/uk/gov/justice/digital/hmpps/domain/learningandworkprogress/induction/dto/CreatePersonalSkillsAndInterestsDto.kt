package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill

data class CreatePersonalSkillsAndInterestsDto(
  val skills: List<PersonalSkill>,
  val interests: List<PersonalInterest>,
  val prisonId: String,
)
