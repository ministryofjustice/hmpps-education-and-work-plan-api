package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import java.util.UUID

data class UpdateInductionDto(
  val reference: UUID?,
  val prisonNumber: String,
  val workOnRelease: UpdateWorkOnReleaseDto?,
  val previousQualifications: UpdatePreviousQualificationsDto?,
  val previousTraining: UpdatePreviousTrainingDto?,
  val previousWorkExperiences: UpdatePreviousWorkExperiencesDto?,
  val inPrisonInterests: UpdateInPrisonInterestsDto?,
  val personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsDto?,
  val futureWorkInterests: UpdateFutureWorkInterestsDto?,
  val prisonId: String,
)
