package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkInterestType
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidCiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidEducationAndQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidPreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidPrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidWorkExperienceResource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidWorkInterestsResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.HighestEducationLevel as HighestEducationLevelEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.HopingToWork as HopingToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonTrainingType as InPrisonTrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonWorkType as InPrisonWorkTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.TrainingType as TrainingTypeEntity

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
      hopingToGetWork = HopingToWork.NOT_SURE,
      desireToWork = false,
      abilityToWork = setOf(AbilityToWorkFactor.OTHER),
      abilityToWorkOther = "Mental health issues",
      reasonToNotGetWork = setOf(ReasonNotToWork.OTHER),
      reasonToNotGetWorkOther = "Crime pays",
      workExperience = aValidPreviousWorkResponse(
        hasWorkedBefore = true,
        typeOfWorkExperience = setOf(WorkType.OTHER),
        typeOfWorkExperienceOther = "Scientist",
        workExperience = setOf(
          aValidWorkExperienceResource(
            typeOfWorkExperience = WorkType.OTHER,
            otherWork = "Scientist",
            role = "Lab Technician",
            details = "Cleaning test tubes",
          ),
        ),
        workInterests = aValidWorkInterestsResponse(
          workInterests = setOf(WorkType.OTHER),
          workInterestsOther = "Any job I can get",
          particularJobInterests = setOf(
            WorkInterestDetail(
              workInterest = WorkType.OTHER,
              role = "Any role",
            ),
          ),
          modifiedBy = "bjones_gen",
          modifiedDateTime = modifiedDateTime,
        ),
        modifiedBy = "bjones_gen",
        modifiedDateTime = modifiedDateTime,
      ),
      skillsAndInterests = aValidSkillsAndInterestsResponse(
        skills = setOf(PersonalSkill.OTHER),
        skillsOther = "Hidden skills",
        personalInterests = setOf(PersonalInterest.OTHER),
        personalInterestsOther = "Secret interests",
        modifiedBy = "bjones_gen",
        modifiedDateTime = modifiedDateTime,
      ),
      qualificationsAndTraining = aValidEducationAndQualificationsResponse(
        educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
        qualifications = setOf(
          aValidAchievedQualification(
            subject = "English",
            level = AchievedQualification.Level.LEVEL_3,
            grade = "A",
          ),
        ),
        additionalTraining = setOf(TrainingType.OTHER),
        additionalTrainingOther = "Kotlin course",
        modifiedBy = "bjones_gen",
        modifiedDateTime = modifiedDateTime,
      ),
      inPrisonInterests = aValidPrisonWorkAndEducationResponse(
        inPrisonWork = setOf(InPrisonWorkType.OTHER),
        inPrisonWorkOther = "Any in-prison work",
        inPrisonEducation = setOf(InPrisonTrainingType.OTHER),
        inPrisonEducationOther = "Any in-prison training",
        modifiedBy = "bjones_gen",
        modifiedDateTime = modifiedDateTime,
      ),
      createdBy = "asmith_gen",
      createdDateTime = createdDateTime,
      modifiedBy = "bjones_gen",
      modifiedDateTime = modifiedDateTime,
    )

    val expectedWorkOnReleaseEntity = aValidWorkOnReleaseMigrationEntity(
      hopingToWork = HopingToWorkEntity.NOT_SURE,
      notHopingToWorkReasons = mutableListOf(NotHopingToWorkReason.OTHER),
      notHopingToWorkOtherReason = "Crime pays",
      affectAbilityToWork = mutableListOf(AffectAbilityToWork.OTHER),
      affectAbilityToWorkOther = "Mental health issues",
      createdAt = createdAt,
      createdAtPrison = prisonId,
      createdBy = "asmith_gen",
      createdByDisplayName = "asmith_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedPreviousQualificationsEntity = aValidPreviousQualificationsMigrationEntity(
      educationLevel = HighestEducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(
        aValidQualificationMigrationEntity(
          subject = "English",
          level = QualificationLevel.LEVEL_3,
          grade = "A",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedPreviousTrainingEntity = aValidPreviousTrainingMigrationEntity(
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER),
      trainingTypeOther = "Kotlin course",
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesMigrationEntity(
      experiences = mutableListOf(
        aValidWorkExperienceMigrationEntity(
          experienceType = WorkExperienceType.OTHER,
          experienceTypeOther = "Scientist",
          role = "Lab Technician",
          details = "Cleaning test tubes",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedInPrisonInterestsEntity = aValidInPrisonInterestsMigrationEntity(
      inPrisonWorkInterests = mutableListOf(
        aValidInPrisonWorkInterestMigrationEntity(
          workType = InPrisonWorkTypeEntity.OTHER,
          workTypeOther = "Any in-prison work",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      inPrisonTrainingInterests = mutableListOf(
        aValidInPrisonTrainingInterestMigrationEntity(
          trainingType = InPrisonTrainingTypeEntity.OTHER,
          trainingTypeOther = "Any in-prison training",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsMigrationEntity(
      skills = mutableListOf(
        aValidPersonalSkillMigrationEntity(
          skillType = SkillType.OTHER,
          skillTypeOther = "Hidden skills",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      interests = mutableListOf(
        aValidPersonalInterestMigrationEntity(
          interestType = InterestType.OTHER,
          interestTypeOther = "Secret interests",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedFutureWorkInterestsEntity = aValidFutureWorkInterestsMigrationEntity(
      interests = mutableListOf(
        aValidWorkInterestMigrationEntity(
          workType = WorkInterestType.OTHER,
          workTypeOther = "Any job I can get",
          role = "Any role",
          createdAt = updatedAt,
          createdBy = "bjones_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        ),
      ),
      createdAt = updatedAt,
      createdAtPrison = prisonId,
      createdBy = "bjones_gen",
      createdByDisplayName = "bjones_gen",
      updatedAt = updatedAt,
      updatedAtPrison = prisonId,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "bjones_gen",
    )
    val expectedInductionMigrationEntity = aValidInductionMigrationEntity(
      prisonNumber = prisonNumber,
      workOnRelease = expectedWorkOnReleaseEntity,
      previousQualifications = expectedPreviousQualificationsEntity,
      previousTraining = expectedPreviousTrainingEntity,
      previousWorkExperiences = expectedPreviousWorkExperiencesEntity,
      inPrisonInterests = expectedInPrisonInterestsEntity,
      personalSkillsAndInterests = expectedPersonalSkillsAndInterestsEntity,
      futureWorkInterests = expectedFutureWorkInterestsEntity,
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
