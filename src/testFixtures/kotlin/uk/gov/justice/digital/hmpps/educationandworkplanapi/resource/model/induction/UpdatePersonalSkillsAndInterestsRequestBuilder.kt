package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePersonalSkillsAndInterestsRequest
import java.util.UUID

fun aValidUpdatePersonalSkillsAndInterestsRequest(
  reference: UUID? = UUID.randomUUID(),
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
): UpdatePersonalSkillsAndInterestsRequest = UpdatePersonalSkillsAndInterestsRequest(
  reference = reference,
  skills = skills,
  interests = interests,
)
