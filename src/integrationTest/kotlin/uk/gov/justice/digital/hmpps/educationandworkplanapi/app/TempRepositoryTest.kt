package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import java.util.UUID

@Deprecated("A temporary IT until we have the REST endpoint in place")
internal class TempRepositoryTest : IntegrationTestBase() {

  @Test
  @Transactional
  fun `should process CIAG induction created event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    // TODO - add test data builder
    val induction = InductionEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      workOnRelease = WorkOnReleaseEntity(
        reference = UUID.randomUUID(),
        hopingToWork = HopingToWork.NO,
        notHopingToWorkReasons = listOf(NotHopingToWorkReason.NOT_SURE),
        notHopingToWorkOtherReason = "Test notHopingToWorkOtherReason",
        affectAbilityToWork = listOf(AffectAbilityToWork.CARING_RESPONSIBILITIES),
        affectAbilityToWorkOther = "Test affectAbilityToWorkOther",
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      previousQualifications = PreviousQualificationsEntity(
        reference = UUID.randomUUID(),
        educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
        qualifications = listOf(
          QualificationEntity(
            reference = UUID.randomUUID(),
            subject = "English",
            level = QualificationLevel.LEVEL_3,
            grade = "A",
          ),
          QualificationEntity(
            reference = UUID.randomUUID(),
            subject = "Maths",
            level = QualificationLevel.LEVEL_4,
            grade = "A",
          ),
        ),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      previousTraining = PreviousTrainingEntity(
        reference = UUID.randomUUID(),
        trainingTypes = listOf(TrainingType.OTHER),
        trainingTypeOther = "Test trainingTypeOther",
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      previousWorkExperiences = PreviousWorkExperiencesEntity(
        reference = UUID.randomUUID(),
        experiences = listOf(
          WorkExperienceEntity(
            reference = UUID.randomUUID(),
            experienceType = WorkExperienceType.DRIVING,
            experienceTypeOther = "Test experienceTypeOther",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse",
          ),
        ),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      inPrisonInterests = InPrisonInterestsEntity(
        reference = UUID.randomUUID(),
        inPrisonWorkInterests = listOf(
          InPrisonWorkInterestEntity(
            reference = UUID.randomUUID(),
            workType = InPrisonWorkType.CLEANING_AND_HYGIENE,
            workTypeOther = "Test workTypeOther",
          ),
        ),
        inPrisonTrainingInterests = listOf(
          InPrisonTrainingInterestEntity(
            reference = UUID.randomUUID(),
            trainingType = InPrisonTrainingType.OTHER,
            trainingTypeOther = "Test trainingTypeOther",
          ),
        ),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      personalSkillsAndInterests = PersonalSkillsAndInterestsEntity(
        reference = UUID.randomUUID(),
        skills = listOf(
          PersonalSkillEntity(
            reference = UUID.randomUUID(),
            skillType = SkillType.COMMUNICATION,
            skillTypeOther = "Test skillTypeOther",
          ),
        ),
        interests = listOf(
          PersonalInterestEntity(
            reference = UUID.randomUUID(),
            interestType = InterestType.COMMUNITY,
            interestTypeOther = "Test interestTypeOther",
          ),
        ),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      futureWorkInterestsEntity = FutureWorkInterestsEntity(
        reference = UUID.randomUUID(),
        interests = listOf(
          WorkInterestEntity(
            reference = UUID.randomUUID(),
            workType = WorkInterestType.BEAUTY,
            workTypeOther = "Test workTypeOther",
            role = "Cutting nails",
          ),
        ),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
    inductionRepository.save(induction)

    // When
    val actual = inductionRepository.findByPrisonNumber(prisonNumber)

    // Then
    // TODO - add custom assertions
    assertThat(actual!!.id).isNotNull()
    assertThat(actual.reference).isNotNull()
    assertThat(actual.createdAt).isNotNull()
    assertThat(actual.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.createdBy).isNotNull()
    assertThat(actual.createdByDisplayName).isNotNull()
    assertThat(actual.updatedAt).isNotNull()
    assertThat(actual.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.updatedBy).isNotNull()
    assertThat(actual.updatedByDisplayName).isNotNull()

    assertThat(actual.workOnRelease).isNotNull()
    assertThat(actual.workOnRelease!!.id).isNotNull()
    assertThat(actual.workOnRelease!!.reference).isNotNull()
    assertThat(actual.workOnRelease!!.hopingToWork).isEqualTo(HopingToWork.NO)
    assertThat(actual.workOnRelease!!.notHopingToWorkReasons).containsExactly(NotHopingToWorkReason.NOT_SURE)
    assertThat(actual.workOnRelease!!.notHopingToWorkOtherReason).isEqualTo("Test notHopingToWorkOtherReason")
    assertThat(actual.workOnRelease!!.affectAbilityToWork).containsExactly(AffectAbilityToWork.CARING_RESPONSIBILITIES)
    assertThat(actual.workOnRelease!!.affectAbilityToWorkOther).isEqualTo("Test affectAbilityToWorkOther")
    assertThat(actual.workOnRelease!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.workOnRelease!!.updatedAtPrison).isEqualTo(prisonId)

    assertThat(actual.previousQualifications).isNotNull()
    assertThat(actual.previousQualifications!!.id).isNotNull()
    assertThat(actual.previousQualifications!!.reference).isNotNull()
    assertThat(actual.previousQualifications!!.educationLevel).isEqualTo(HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(actual.previousQualifications!!.qualifications).hasSize(2)
    assertThat(actual.previousQualifications!!.createdAt).isNotNull()
    assertThat(actual.previousQualifications!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousQualifications!!.createdBy).isNotNull()
    assertThat(actual.previousQualifications!!.createdByDisplayName).isNotNull()
    assertThat(actual.previousQualifications!!.updatedAt).isNotNull()
    assertThat(actual.previousQualifications!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousQualifications!!.updatedBy).isNotNull()
    assertThat(actual.previousQualifications!!.updatedByDisplayName).isNotNull()

    assertThat(actual.previousTraining).isNotNull()
    assertThat(actual.previousTraining!!.id).isNotNull()
    assertThat(actual.previousTraining!!.reference).isNotNull()
    assertThat(actual.previousTraining!!.trainingTypes).containsExactly(TrainingType.OTHER)
    assertThat(actual.previousTraining!!.trainingTypeOther).isEqualTo("Test trainingTypeOther")
    assertThat(actual.previousTraining!!.createdAt).isNotNull()
    assertThat(actual.previousTraining!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousTraining!!.createdBy).isNotNull()
    assertThat(actual.previousTraining!!.createdByDisplayName).isNotNull()
    assertThat(actual.previousTraining!!.updatedAt).isNotNull()
    assertThat(actual.previousTraining!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousTraining!!.updatedBy).isNotNull()
    assertThat(actual.previousTraining!!.updatedByDisplayName).isNotNull()

    assertThat(actual.previousWorkExperiences).isNotNull()
    assertThat(actual.previousWorkExperiences!!.id).isNotNull()
    assertThat(actual.previousWorkExperiences!!.reference).isNotNull()
    assertThat(actual.previousWorkExperiences!!.experiences).hasSize(1)
    assertThat(actual.previousWorkExperiences!!.createdAt).isNotNull()
    assertThat(actual.previousWorkExperiences!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousWorkExperiences!!.createdBy).isNotNull()
    assertThat(actual.previousWorkExperiences!!.createdByDisplayName).isNotNull()
    assertThat(actual.previousWorkExperiences!!.updatedAt).isNotNull()
    assertThat(actual.previousWorkExperiences!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.previousWorkExperiences!!.updatedBy).isNotNull()
    assertThat(actual.previousWorkExperiences!!.updatedByDisplayName).isNotNull()

    assertThat(actual.inPrisonInterests).isNotNull()
    assertThat(actual.inPrisonInterests!!.id).isNotNull()
    assertThat(actual.inPrisonInterests!!.reference).isNotNull()
    assertThat(actual.inPrisonInterests!!.inPrisonWorkInterests).hasSize(1)
    assertThat(actual.inPrisonInterests!!.inPrisonTrainingInterests).hasSize(1)
    assertThat(actual.inPrisonInterests!!.createdAt).isNotNull()
    assertThat(actual.inPrisonInterests!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.inPrisonInterests!!.createdBy).isNotNull()
    assertThat(actual.inPrisonInterests!!.createdByDisplayName).isNotNull()
    assertThat(actual.inPrisonInterests!!.updatedAt).isNotNull()
    assertThat(actual.inPrisonInterests!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.inPrisonInterests!!.updatedBy).isNotNull()
    assertThat(actual.inPrisonInterests!!.updatedByDisplayName).isNotNull()

    assertThat(actual.personalSkillsAndInterests).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.id).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.reference).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.skills).hasSize(1)
    assertThat(actual.personalSkillsAndInterests!!.interests).hasSize(1)
    assertThat(actual.personalSkillsAndInterests!!.createdAt).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.personalSkillsAndInterests!!.createdBy).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.createdByDisplayName).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.updatedAt).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.personalSkillsAndInterests!!.updatedBy).isNotNull()
    assertThat(actual.personalSkillsAndInterests!!.updatedByDisplayName).isNotNull()

    assertThat(actual.futureWorkInterestsEntity).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.id).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.reference).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.interests).hasSize(1)
    assertThat(actual.futureWorkInterestsEntity!!.createdAt).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.futureWorkInterestsEntity!!.createdBy).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.createdByDisplayName).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.updatedAt).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.futureWorkInterestsEntity!!.updatedBy).isNotNull()
    assertThat(actual.futureWorkInterestsEntity!!.updatedByDisplayName).isNotNull()
  }
}
