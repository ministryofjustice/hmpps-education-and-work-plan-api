package uk.gov.justice.digital.hmpps.domain.induction.dto

data class CreateInductionDto(
  val prisonNumber: String,
  val workOnRelease: CreateWorkOnReleaseDto,
  val previousQualifications: CreatePreviousQualificationsDto?,
  val previousTraining: CreatePreviousTrainingDto?,
  val previousWorkExperiences: CreatePreviousWorkExperiencesDto?,
  val inPrisonInterests: CreateInPrisonInterestsDto?,
  val personalSkillsAndInterests: CreatePersonalSkillsAndInterestsDto?,
  val futureWorkInterests: CreateFutureWorkInterestsDto?,
  val prisonId: String,
)
