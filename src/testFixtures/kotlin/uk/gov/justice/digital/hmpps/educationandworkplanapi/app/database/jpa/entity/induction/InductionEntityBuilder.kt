package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.util.UUID

fun aValidInductionEntity(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  workOnRelease: WorkOnReleaseEntity = aValidWorkOnReleaseEntity(),
  previousQualifications: PreviousQualificationsEntity = aValidPreviousQualificationsEntity(),
  previousTraining: PreviousTrainingEntity = aValidPreviousTrainingEntity(),
  previousWorkExperiences: PreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(),
  inPrisonInterests: InPrisonInterestsEntity = aValidInPrisonInterestsEntity(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(),
  futureWorkInterestsEntity: FutureWorkInterestsEntity = aValidFutureWorkInterestsEntity(),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) = InductionEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterestsEntity = futureWorkInterestsEntity,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
)
