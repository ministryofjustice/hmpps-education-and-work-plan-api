package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperiencesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkOnReleaseResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidInductionResponse(): InductionResponse = aValidInductionResponseForPrisonerNotLookingToWork()

fun aValidInductionResponseForPrisonerNotLookingToWork(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnReleaseResponse = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork(),
  previousQualifications: PreviousQualificationsResponse? = aValidPreviousQualificationsResponse(
    educationLevel = HighestEducationLevel.NOT_SURE,
  ),
  previousTraining: PreviousTrainingResponse? = aValidPreviousTrainingResponse(),
  previousWorkExperiences: PreviousWorkExperiencesResponse? = null,
  inPrisonInterests: InPrisonInterestsResponse? = aValidInPrisonInterestsResponse(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsResponse? = null,
  futureWorkInterests: FutureWorkInterestsResponse? = null,
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): InductionResponse = InductionResponse(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)

fun aValidInductionResponseForPrisonerLookingToWork(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnReleaseResponse = aValidWorkOnReleaseResponseForPrisonerLookingToWork(),
  previousQualifications: PreviousQualificationsResponse? = aValidPreviousQualificationsResponse(),
  previousTraining: PreviousTrainingResponse? = aValidPreviousTrainingResponse(),
  previousWorkExperiences: PreviousWorkExperiencesResponse? = aValidPreviousWorkExperiencesResponse(),
  inPrisonInterests: InPrisonInterestsResponse? = null,
  personalSkillsAndInterests: PersonalSkillsAndInterestsResponse? = aValidPersonalSkillsAndInterestsResponse(),
  futureWorkInterests: FutureWorkInterestsResponse? = aValidFutureWorkInterestsResponse(),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): InductionResponse = InductionResponse(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)

fun aFullyPopulatedInductionResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnReleaseResponse = aValidWorkOnReleaseResponseForPrisonerLookingToWork(),
  previousQualifications: PreviousQualificationsResponse? = aValidPreviousQualificationsResponse(),
  previousTraining: PreviousTrainingResponse? = aValidPreviousTrainingResponse(),
  previousWorkExperiences: PreviousWorkExperiencesResponse? = aValidPreviousWorkExperiencesResponse(),
  inPrisonInterests: InPrisonInterestsResponse? = aValidInPrisonInterestsResponse(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsResponse? = aValidPersonalSkillsAndInterestsResponse(),
  futureWorkInterests: FutureWorkInterestsResponse? = aValidFutureWorkInterestsResponse(),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): InductionResponse = InductionResponse(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
