package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidInductionMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  workOnRelease: WorkOnReleaseMigrationEntity = aValidWorkOnReleaseMigrationEntity(),
  previousQualifications: PreviousQualificationsMigrationEntity = aValidPreviousQualificationsMigrationEntity(),
  previousTraining: PreviousTrainingMigrationEntity = aValidPreviousTrainingMigrationEntity(),
  previousWorkExperiences: PreviousWorkExperiencesMigrationEntity = aValidPreviousWorkExperiencesMigrationEntity(),
  inPrisonInterests: InPrisonInterestsMigrationEntity = aValidInPrisonInterestsMigrationEntity(),
  personalSkillsAndInterests: PersonalSkillsAndInterestsMigrationEntity = aValidPersonalSkillsAndInterestsMigrationEntity(),
  futureWorkInterests: FutureWorkInterestsMigrationEntity = aValidFutureWorkInterestsMigrationEntity(),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) = InductionMigrationEntity(
  id = id,
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
)
