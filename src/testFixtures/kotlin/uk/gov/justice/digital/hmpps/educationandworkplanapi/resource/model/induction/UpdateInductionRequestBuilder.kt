package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkOnReleaseRequest
import java.util.UUID

fun aValidUpdateInductionRequest(): UpdateInductionRequest = aValidUpdateInductionRequestForPrisonerNotLookingToWork()

fun aValidUpdateInductionRequestForPrisonerNotLookingToWork(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
  workOnRelease: UpdateWorkOnReleaseRequest = aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork(),
  previousQualifications: UpdatePreviousQualificationsRequest? = aValidUpdatePreviousQualificationsRequest(
    educationLevel = null,
  ),
  previousTraining: UpdatePreviousTrainingRequest? = aValidUpdatePreviousTrainingRequest(),
  previousWorkExperiences: UpdatePreviousWorkExperiencesRequest? = null,
  inPrisonInterests: UpdateInPrisonInterestsRequest? = aValidUpdateInPrisonInterestsRequest(),
  personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsRequest? = null,
  futureWorkInterests: UpdateFutureWorkInterestsRequest? = null,
): UpdateInductionRequest = UpdateInductionRequest(
  reference = reference,
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
)

fun aValidUpdateInductionRequestForPrisonerLookingToWork(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
  workOnRelease: UpdateWorkOnReleaseRequest = aValidUpdateWorkOnReleaseRequestForPrisonerLookingToWork(),
  previousQualifications: UpdatePreviousQualificationsRequest? = aValidUpdatePreviousQualificationsRequest(),
  previousTraining: UpdatePreviousTrainingRequest? = aValidUpdatePreviousTrainingRequest(),
  previousWorkExperiences: UpdatePreviousWorkExperiencesRequest? = aValidUpdatePreviousWorkExperiencesRequest(),
  inPrisonInterests: UpdateInPrisonInterestsRequest? = null,
  personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsRequest? = aValidUpdatePersonalSkillsAndInterestsRequest(),
  futureWorkInterests: UpdateFutureWorkInterestsRequest? = aValidUpdateFutureWorkInterestsRequest(),
): UpdateInductionRequest = UpdateInductionRequest(
  reference = reference,
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
)

fun aFullyPopulatedUpdateInductionRequest(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
  workOnRelease: UpdateWorkOnReleaseRequest = aValidUpdateWorkOnReleaseRequest(),
  previousQualifications: UpdatePreviousQualificationsRequest? = aValidUpdatePreviousQualificationsRequest(),
  previousTraining: UpdatePreviousTrainingRequest? = aValidUpdatePreviousTrainingRequest(),
  previousWorkExperiences: UpdatePreviousWorkExperiencesRequest? = aValidUpdatePreviousWorkExperiencesRequest(),
  inPrisonInterests: UpdateInPrisonInterestsRequest? = aValidUpdateInPrisonInterestsRequest(),
  personalSkillsAndInterests: UpdatePersonalSkillsAndInterestsRequest? = aValidUpdatePersonalSkillsAndInterestsRequest(),
  futureWorkInterests: UpdateFutureWorkInterestsRequest? = aValidUpdateFutureWorkInterestsRequest(),
): UpdateInductionRequest = UpdateInductionRequest(
  reference = reference,
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
)
