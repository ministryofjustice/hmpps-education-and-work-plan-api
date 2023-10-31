package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType
import java.util.UUID

data class UpdatePreviousTrainingDto(
  val reference: UUID,
  val trainingTypes: List<TrainingType>,
  val trainingTypeOther: String?,
  val prisonId: String,
)
