package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.anotherValidAchievedQualificationResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidPreviousQualificationsResponse(
  reference: UUID = UUID.randomUUID(),
  educationLevel: EducationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<AchievedQualificationResponse> = listOf(
    aValidAchievedQualificationResponse(),
    anotherValidAchievedQualificationResponse(),
  ),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): PreviousQualificationsResponse = PreviousQualificationsResponse(
  reference = reference,
  educationLevel = educationLevel,
  qualifications = qualifications,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
