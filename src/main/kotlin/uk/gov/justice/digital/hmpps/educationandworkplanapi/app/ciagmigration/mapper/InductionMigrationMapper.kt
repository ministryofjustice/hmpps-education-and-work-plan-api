package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InductionMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.CiagInductionResponse
import java.time.ZoneOffset
import java.util.UUID

@Component
class InductionMigrationMapper(
  private val workOnReleaseMapper: WorkOnReleaseMigrationMapper,
  private val qualificationsMapper: PreviousQualificationsMigrationMapper,
  private val trainingMapper: PreviousTrainingMigrationMapper,
  private val workExperiencesMapper: PreviousWorkExperiencesMigrationMapper,
  private val inPrisonInterestsMapper: InPrisonInterestsMigrationMapper,
  private val skillsAndInterestsMapper: SkillsAndInterestsMigrationMapper,
  private val workInterestsMapper: FutureWorkInterestsMigrationMapper,
) {
  fun toInductionMigrationEntity(ciagInduction: CiagInductionResponse): InductionMigrationEntity =
    with(ciagInduction) {
      // prisonId shouldn't be null, but we've seen bugs here on the CIAG side recently.
      val prisonId = ciagInduction.prisonId ?: ""
      InductionMigrationEntity(
        reference = UUID.randomUUID(),
        prisonNumber = offenderId,
        workOnRelease = workOnReleaseMapper.toWorkOnReleaseMigrationEntity(prisonId, this),
        previousQualifications = qualificationsMapper.toPreviousQualificationsMigrationEntity(
          prisonId,
          qualificationsAndTraining,
        ),
        previousTraining = trainingMapper.toPreviousTrainingMigrationEntity(
          prisonId,
          qualificationsAndTraining,
        ),
        previousWorkExperiences = workExperiencesMapper.toPreviousWorkExperiencesMigrationEntity(
          prisonId,
          workExperience,
        ),
        inPrisonInterests = inPrisonInterestsMapper.toInPrisonInterestsMigrationEntity(
          prisonId,
          inPrisonInterests,
        ),
        personalSkillsAndInterests = skillsAndInterestsMapper.toPersonalSkillsAndInterestsMigrationEntity(
          prisonId,
          skillsAndInterests,
        ),
        futureWorkInterests = workInterestsMapper.toFutureWorkInterestsMigrationEntity(
          prisonId,
          workExperience?.workInterests,
        ),
        createdAt = createdDateTime.toInstant(ZoneOffset.UTC),
        createdAtPrison = prisonId,
        createdBy = createdBy,
        createdByDisplayName = createdBy,
        updatedAt = modifiedDateTime.toInstant(ZoneOffset.UTC),
        updatedAtPrison = prisonId,
        updatedBy = modifiedBy,
        updatedByDisplayName = modifiedBy,
      )
    }
}
