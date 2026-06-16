package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillsResponse
import java.time.OffsetDateTime

fun aValidEmployabilitySkillResponse(
  employabilitySkillType: EmployabilitySkillType = EmployabilitySkillType.COMMUNICATION,
  employabilitySkillRating: EmployabilitySkillRating = EmployabilitySkillRating.VERY_CONFIDENT,
  evidence: String = "evidence",
  sessionType: EmployabilitySkillSessionType? = EmployabilitySkillSessionType.CIAG_INDUCTION,
  sessionTypeDescription: String? = "Maths class",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): GetEmployabilitySkillsResponse = GetEmployabilitySkillsResponse(
  employabilitySkillType = employabilitySkillType,
  employabilitySkillRating = employabilitySkillRating,
  evidence = evidence,
  sessionType = sessionType,
  sessionTypeDescription = sessionTypeDescription,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
