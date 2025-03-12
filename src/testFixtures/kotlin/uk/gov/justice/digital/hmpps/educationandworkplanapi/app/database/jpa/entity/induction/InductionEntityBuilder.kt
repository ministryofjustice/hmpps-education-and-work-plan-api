package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidInductionEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  workOnRelease: WorkOnReleaseEntity = aValidWorkOnReleaseEntity(),
  previousTraining: PreviousTrainingEntity = aValidPreviousTrainingEntity(),
  previousWorkExperiences: PreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(),
  inPrisonInterests: InPrisonInterestsEntity = aValidInPrisonInterestsEntity(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(),
  futureWorkInterests: FutureWorkInterestsEntity = aValidFutureWorkInterestsEntity(),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  conductedBy: String? = null,
  conductedByRole: String? = null,
  completedDate: LocalDate? = LocalDate.now(),
) = InductionEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
  conductedBy = conductedBy,
  conductedByRole = conductedByRole,
  completedDate = completedDate,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidInductionEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  workOnRelease: WorkOnReleaseEntity = aValidWorkOnReleaseEntityWithJpaFieldsPopulated(),
  previousTraining: PreviousTrainingEntity = aValidPreviousTrainingEntityWithJpaFieldsPopulated(),
  previousWorkExperiences: PreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(),
  inPrisonInterests: InPrisonInterestsEntity = aValidInPrisonInterestsEntityWithJpaFieldsPopulated(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(),
  futureWorkInterests: FutureWorkInterestsEntity = aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  conductedBy: String? = "John Smith",
  conductedByRole: String? = "Peer Mentor",
  completedDate: LocalDate? = LocalDate.now(),
) = InductionEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
  conductedBy = conductedBy,
  conductedByRole = conductedByRole,
  completedDate = completedDate,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
