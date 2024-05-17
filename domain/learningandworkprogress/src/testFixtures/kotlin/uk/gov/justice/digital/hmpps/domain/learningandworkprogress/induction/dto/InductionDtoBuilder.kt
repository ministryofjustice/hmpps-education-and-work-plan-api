package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import java.util.UUID

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

fun aValidUpdateInductionDto(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: UpdateWorkOnReleaseDto = aValidUpdateWorkOnReleaseDto(),
  previousQualifications: UpdatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(),
  previousTraining: UpdatePreviousTrainingDto = aValidUpdatePreviousTrainingDto(),
  previousWorkExperiences: UpdatePreviousWorkExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(),
  inPrisonInterests: UpdateInPrisonInterestsDto = aValidUpdateInPrisonInterestsDto(),
  personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(),
  futureWorkInterests: UpdateFutureWorkInterestsDto = aValidUpdateFutureWorkInterestsDto(),
  prisonId: String = "BXI",
) = UpdateInductionDto(
  reference = reference,
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
