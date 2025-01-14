package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiences(
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PreviousWorkExperiences(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
    experiences = experiences,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidWorkExperience(
  experienceType: WorkExperienceType = WorkExperienceType.OTHER,
  experienceTypeOther: String? = "All sorts",
  role: String = "General dog's body",
  details: String? = null,
) =
  WorkExperience(
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
