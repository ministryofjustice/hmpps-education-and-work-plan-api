package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkill
import java.util.UUID

fun aValidCreatePersonalSkillsAndInterestsDto(
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
  prisonId: String = "BXI",
) = CreatePersonalSkillsAndInterestsDto(
  skills = skills,
  interests = interests,
  prisonId = prisonId,
)

fun aValidUpdatePersonalSkillsAndInterestsDto(
  reference: UUID = UUID.randomUUID(),
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
  prisonId: String = "BXI",
) = UpdatePersonalSkillsAndInterestsDto(
  reference = reference,
  skills = skills,
  interests = interests,
  prisonId = prisonId,
)
