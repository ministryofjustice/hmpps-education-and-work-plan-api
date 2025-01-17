package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import java.time.LocalDate

data class CreateInductionDto(
  val prisonNumber: String,
  val workOnRelease: CreateWorkOnReleaseDto,
  val previousQualifications: CreatePreviousQualificationsDto?,
  val previousTraining: CreatePreviousTrainingDto,
  val previousWorkExperiences: CreatePreviousWorkExperiencesDto?,
  val inPrisonInterests: CreateInPrisonInterestsDto?,
  val personalSkillsAndInterests: CreatePersonalSkillsAndInterestsDto?,
  val futureWorkInterests: CreateFutureWorkInterestsDto?,
  val prisonId: String,
  val conductedAt: LocalDate?,
  val conductedBy: String? = null,
  val conductedByRole: String? = null,
  val note: String? = null,
)
