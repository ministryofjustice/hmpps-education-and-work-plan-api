package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiences(
  reference: UUID = UUID.randomUUID(),
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PreviousWorkExperiences(
    reference = reference,
    experiences = experiences,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
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
