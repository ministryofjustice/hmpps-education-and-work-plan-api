package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import java.util.UUID

data class UpdatePreviousWorkExperiencesDto(
  val reference: UUID?,
  val hasWorkedBefore: HasWorkedBefore,
  val hasWorkedBeforeNotRelevantReason: String?,
  val experiences: List<WorkExperience>,
  val prisonId: String,
)
