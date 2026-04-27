package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill

import java.time.Instant
import java.util.UUID

fun aValidEmployabilitySkill(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  employabilitySkillType: EmployabilitySkillType = EmployabilitySkillType.COMMUNICATION,
  rating: EmployabilitySkillRating = EmployabilitySkillRating.QUITE_CONFIDENT,
  evidence: String = "Demonstrated in class",
  sessionType: EmployabilitySkillSessionType? = null,
  sessionTypeDescription: String? = null,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "bjones_gen",
  updatedAt: Instant = Instant.now(),
  updatedAtPrison: String = "BXI",
): EmployabilitySkill = EmployabilitySkill(
  reference = reference,
  prisonNumber = prisonNumber,
  employabilitySkillType = employabilitySkillType,
  rating = rating,
  evidence = evidence,
  sessionType = sessionType,
  sessionTypeDescription = sessionTypeDescription,
  createdBy = createdBy,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
