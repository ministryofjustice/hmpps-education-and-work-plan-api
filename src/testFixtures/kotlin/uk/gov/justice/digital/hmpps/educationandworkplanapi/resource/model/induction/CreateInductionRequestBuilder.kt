package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest
import java.time.LocalDate

fun aValidCreateInductionRequest(): CreateInductionRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork()

fun aValidCreateInductionRequestForPrisonerNotLookingToWork(
  prisonId: String = "BXI",
  workOnRelease: CreateWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequestForPrisonerNotLookingToWork(),
  previousQualifications: CreatePreviousQualificationsRequest? = aValidCreatePreviousQualificationsRequest(
    educationLevel = null,
  ),
  previousTraining: CreatePreviousTrainingRequest? = aValidCreatePreviousTrainingRequest(),
  previousWorkExperiences: CreatePreviousWorkExperiencesRequest? = null,
  inPrisonInterests: CreateInPrisonInterestsRequest? = aValidCreateInPrisonInterestsRequest(),
  personalSkillsAndInterests: CreatePersonalSkillsAndInterestsRequest? = null,
  futureWorkInterests: CreateFutureWorkInterestsRequest? = null,
  conductedAt: LocalDate? = null,
  conductedBy: String? = null,
  conductedByRole: String? = null,
): CreateInductionRequest = CreateInductionRequest(
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  conductedAt = conductedAt,
  conductedBy = conductedBy,
  conductedByRole = conductedByRole,

)

fun aValidCreateInductionRequestForPrisonerLookingToWork(
  prisonId: String = "BXI",
  workOnRelease: CreateWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequestForPrisonerLookingToWork(),
  previousQualifications: CreatePreviousQualificationsRequest? = aValidCreatePreviousQualificationsRequest(),
  previousTraining: CreatePreviousTrainingRequest? = aValidCreatePreviousTrainingRequest(),
  previousWorkExperiences: CreatePreviousWorkExperiencesRequest? = aValidCreatePreviousWorkExperiencesRequest(),
  inPrisonInterests: CreateInPrisonInterestsRequest? = null,
  personalSkillsAndInterests: CreatePersonalSkillsAndInterestsRequest? = aValidCreatePersonalSkillsAndInterestsRequest(),
  futureWorkInterests: CreateFutureWorkInterestsRequest? = aValidCreateFutureWorkInterestsRequest(),
): CreateInductionRequest = CreateInductionRequest(
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
)

fun aFullyPopulatedCreateInductionRequest(
  prisonId: String = "BXI",
  workOnRelease: CreateWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequest(),
  previousQualifications: CreatePreviousQualificationsRequest? = aValidCreatePreviousQualificationsRequest(),
  previousTraining: CreatePreviousTrainingRequest? = aValidCreatePreviousTrainingRequest(),
  previousWorkExperiences: CreatePreviousWorkExperiencesRequest? = aValidCreatePreviousWorkExperiencesRequest(),
  inPrisonInterests: CreateInPrisonInterestsRequest? = aValidCreateInPrisonInterestsRequest(),
  personalSkillsAndInterests: CreatePersonalSkillsAndInterestsRequest? = aValidCreatePersonalSkillsAndInterestsRequest(),
  futureWorkInterests: CreateFutureWorkInterestsRequest? = aValidCreateFutureWorkInterestsRequest(),
  conductedAt: LocalDate = LocalDate.now(),
  conductedByRole: String? = "Peer Mentor",
  conductedBy: String? = "John Smith",
  note: String? = "example note",
): CreateInductionRequest = CreateInductionRequest(
  prisonId = prisonId,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  conductedAt = conductedAt,
  conductedByRole = conductedByRole,
  conductedBy = conductedBy,
  note = note,

)
