package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperiencesResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidPreviousWorkExperiencesResponse(
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  experiences: List<PreviousWorkExperience> = listOf(aValidPreviousWorkExperience()),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): PreviousWorkExperiencesResponse =
  PreviousWorkExperiencesResponse(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    experiences = experiences,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
  )
