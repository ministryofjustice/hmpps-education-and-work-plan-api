package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidEducationAssessmentEvent(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  statusChangeDate: LocalDate = LocalDate.now(),
  status: EducationAssessmentStatus = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
  source: String = "CURIOUS",
  detailUrl: String? = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "asmith_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) = EducationAssessmentEvent(
  reference = reference,
  prisonNumber = prisonNumber,
  statusChangeDate = statusChangeDate,
  status = status,
  source = source,
  detailUrl = detailUrl,
  createdBy = createdBy,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedAt = lastUpdatedAt,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
)
