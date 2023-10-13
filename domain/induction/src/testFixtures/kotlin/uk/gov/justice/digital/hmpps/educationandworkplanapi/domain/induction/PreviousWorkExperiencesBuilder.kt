package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

fun aValidPreviousWorkExperiences(
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) =
  PreviousWorkExperiences(
    experiences = experiences,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
  )

fun aValidWorkExperience(
  experienceType: WorkExperienceType = WorkExperienceType.CONSTRUCTION,
  experienceTypeOther: String? = null,
  role: String = "Forklift truck driver",
  details: String? = null,
) =
  WorkExperience(
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
