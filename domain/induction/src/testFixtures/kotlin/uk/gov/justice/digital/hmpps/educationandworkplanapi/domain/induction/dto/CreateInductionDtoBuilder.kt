package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

fun aValidCreateInductionDto(
  prisonNumber: String = "A1234AB",
  workOnRelease: CreateWorkOnReleaseDto = aValidCreateWorkOnReleaseDto(),
  previousQualifications: CreatePreviousQualificationsDto = aValidCreatePreviousQualificationsDto(),
  previousTraining: CreatePreviousTrainingDto = aValidCreatePreviousTrainingDto(),
  previousWorkExperiences: CreatePreviousWorkExperiencesDto = aValidCreatePreviousWorkExperiencesDto(),
  inPrisonInterests: CreateInPrisonInterestsDto = aValidCreateInPrisonInterestsDto(),
  personalSkillsAndInterests: CreatePersonalSkillsAndInterestsDto = aValidCreatePersonalSkillsAndInterestsDto(),
  futureWorkInterests: CreateFutureWorkInterestsDto = aValidCreateFutureWorkInterestsDto(),
  prisonId: String = "BXI",
) = CreateInductionDto(
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  prisonId = prisonId,
)
