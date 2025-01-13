package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import java.time.LocalDate
import java.util.UUID

fun aValidCreateInductionDto(
  prisonNumber: String = "A1234AB",
  workOnRelease: CreateWorkOnReleaseDto = aValidCreateWorkOnReleaseDto(),
  previousQualifications: CreatePreviousQualificationsDto? = aValidCreatePreviousQualificationsDto(),
  previousTraining: CreatePreviousTrainingDto = aValidCreatePreviousTrainingDto(),
  previousWorkExperiences: CreatePreviousWorkExperiencesDto = aValidCreatePreviousWorkExperiencesDto(),
  inPrisonInterests: CreateInPrisonInterestsDto = aValidCreateInPrisonInterestsDto(),
  personalSkillsAndInterests: CreatePersonalSkillsAndInterestsDto = aValidCreatePersonalSkillsAndInterestsDto(),
  futureWorkInterests: CreateFutureWorkInterestsDto = aValidCreateFutureWorkInterestsDto(),
  prisonId: String = "BXI",
  conductedAt: LocalDate = LocalDate.now(),
  conductedBy: String? = null,
  conductedByRole: String? = null,
  note: String = "example note",
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
  conductedAt = conductedAt,
  conductedByRole = conductedByRole,
  conductedBy = conductedBy,
  note = note,
)

fun aValidUpdateInductionDto(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: UpdateWorkOnReleaseDto = aValidUpdateWorkOnReleaseDto(),
  previousQualifications: UpdatePreviousQualificationsDto? = aValidUpdatePreviousQualificationsDto(),
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
