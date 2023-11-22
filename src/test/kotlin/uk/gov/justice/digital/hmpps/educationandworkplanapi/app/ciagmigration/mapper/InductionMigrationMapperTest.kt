package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidFutureWorkInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidInPrisonInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidInPrisonTrainingInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidInPrisonWorkInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidInductionMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPersonalInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPersonalSkillMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPersonalSkillsAndInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPreviousQualificationsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPreviousTrainingMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidPreviousWorkExperiencesMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidQualificationMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidWorkExperienceMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidWorkInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidWorkOnReleaseMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidCiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidWorkInterestsResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

class InductionMigrationMapperTest {

  private val localDateTimeMapper = LocalDateTimeMapper()
  private val workOnReleaseMapper = WorkOnReleaseMigrationMapperImpl().also {
    WorkOnReleaseMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val qualificationsMapper = PreviousQualificationsMigrationMapperImpl().also {
    PreviousQualificationsMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val trainingMapper = PreviousTrainingMigrationMapperImpl().also {
    PreviousTrainingMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val workExperiencesMapper = PreviousWorkExperiencesMigrationMapperImpl().also {
    PreviousWorkExperiencesMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val inPrisonInterestsMapper = InPrisonInterestsMigrationMapperImpl().also {
    InPrisonInterestsMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val skillsAndInterestsMapper = SkillsAndInterestsMigrationMapperImpl().also {
    SkillsAndInterestsMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }
  private val workInterestsMapper = FutureWorkInterestsMigrationMapperImpl().also {
    FutureWorkInterestsMigrationMapperImpl::class.java.getDeclaredField("localDateTimeMapper").apply {
      isAccessible = true
      set(it, localDateTimeMapper)
    }
  }

  private val inductionMapper: InductionMigrationMapper = InductionMigrationMapper(
    workOnReleaseMapper = workOnReleaseMapper,
    qualificationsMapper = qualificationsMapper,
    trainingMapper = trainingMapper,
    workExperiencesMapper = workExperiencesMapper,
    inPrisonInterestsMapper = inPrisonInterestsMapper,
    skillsAndInterestsMapper = skillsAndInterestsMapper,
    workInterestsMapper = workInterestsMapper,
  )

  @Test
  fun `should map to InductionMigrationEntity`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
    val updatedAt = Instant.now()
    val createdDateTime = LocalDateTime.ofInstant(createdAt, UTC)
    val modifiedDateTime = LocalDateTime.ofInstant(updatedAt, UTC)

    val ciagInduction = aValidCiagInductionResponse(
      offenderId = prisonNumber,
      prisonId = prisonId,
      workExperience = aValidPreviousWorkResponse(
        workInterests = aValidWorkInterestsResponse(
          modifiedDateTime = modifiedDateTime,
        ),
        modifiedDateTime = modifiedDateTime,
      ),
      skillsAndInterests = aValidSkillsAndInterestsResponse(
        modifiedDateTime = modifiedDateTime,
      ),
      qualificationsAndTraining = aValidEducationAndQualificationsResponse(
        modifiedDateTime = modifiedDateTime,
      ),
      inPrisonInterests = aValidPrisonWorkAndEducationResponse(
        modifiedDateTime = modifiedDateTime,
      ),
      createdBy = "asmith_gen",
      createdDateTime = createdDateTime,
      modifiedBy = "bjones_gen",
      modifiedDateTime = modifiedDateTime,
    )

    val expectedInductionMigrationEntity = aValidInductionMigrationEntity(
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnReleaseMigrationEntity(
        createdAt = createdAt,
        createdBy = "asmith_gen",
        createdByDisplayName = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      previousQualifications = aValidPreviousQualificationsMigrationEntity(
        qualifications = mutableListOf(
          aValidQualificationMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      previousTraining = aValidPreviousTrainingMigrationEntity(
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      previousWorkExperiences = aValidPreviousWorkExperiencesMigrationEntity(
        experiences = mutableListOf(
          aValidWorkExperienceMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      inPrisonInterests = aValidInPrisonInterestsMigrationEntity(
        inPrisonWorkInterests = mutableListOf(
          aValidInPrisonWorkInterestMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        inPrisonTrainingInterests = mutableListOf(
          aValidInPrisonTrainingInterestMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      personalSkillsAndInterests = aValidPersonalSkillsAndInterestsMigrationEntity(
        skills = mutableListOf(
          aValidPersonalSkillMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        interests = mutableListOf(
          aValidPersonalInterestMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      futureWorkInterests = aValidFutureWorkInterestsMigrationEntity(
        interests = mutableListOf(
          aValidWorkInterestMigrationEntity(
            createdAt = updatedAt,
            createdBy = "bjones_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAt = updatedAt,
        createdBy = "bjones_gen",
        createdByDisplayName = "bjones_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
        updatedByDisplayName = "bjones_gen",
      ),
      createdAt = createdAt,
      createdAtPrison = prisonId,
      createdBy = "asmith_gen",
      createdByDisplayName = "asmith_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )

    // When
    val actual = inductionMapper.toInductionMigrationEntity(ciagInduction)

    // Then
    assertThat(actual).usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*id", ".*reference", ".*parent")
      .isEqualTo(expectedInductionMigrationEntity)
  }
}
