package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.EducationAssessmentStatus
import java.time.LocalDate

fun aValidCreateEducationAssessmentEventDto(
  prisonNumber: String = "A1234AB",
  statusChangeDate: LocalDate = LocalDate.now(),
  status: EducationAssessmentStatus = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
  source: String = "CURIOUS",
  detailUrl: String? = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
  prisonId: String = "BXI",
) = CreateEducationAssessmentEventDto(
  prisonNumber = prisonNumber,
  statusChangeDate = statusChangeDate,
  status = status,
  source = source,
  detailUrl = detailUrl,
  prisonId = prisonId,
)
