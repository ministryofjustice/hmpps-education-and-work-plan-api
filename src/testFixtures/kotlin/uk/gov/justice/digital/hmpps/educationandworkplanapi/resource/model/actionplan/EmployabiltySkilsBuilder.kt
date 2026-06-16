package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType

fun aValidCreateEmployabilitySkillRequest(
  prisonId: String = "BXI",
  employabilitySkillType: EmployabilitySkillType = EmployabilitySkillType.COMMUNICATION,
  employabilitySkillRating: EmployabilitySkillRating = EmployabilitySkillRating.VERY_CONFIDENT,
  evidence: String = "evidence",
  sessionType: EmployabilitySkillSessionType? = EmployabilitySkillSessionType.CIAG_INDUCTION,
  sessionTypeDescription: String? = "Maths class",
) = CreateEmployabilitySkillRequest(
  prisonId = prisonId,
  employabilitySkillType = employabilitySkillType,
  employabilitySkillRating = employabilitySkillRating,
  evidence = evidence,
  sessionType = sessionType,
  sessionTypeDescription = sessionTypeDescription,
)
