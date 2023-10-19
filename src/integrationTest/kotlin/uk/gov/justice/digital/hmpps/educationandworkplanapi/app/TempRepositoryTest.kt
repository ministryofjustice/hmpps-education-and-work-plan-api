package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity

@Deprecated("A temporary IT until we have the REST endpoint in place")
internal class TempRepositoryTest : IntegrationTestBase() {

  @Test
  @Transactional
  fun `should process CIAG induction created event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val induction = aValidInductionEntity(prisonNumber = prisonNumber)
    inductionRepository.save(induction)
    val prisonId = "BXI"

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
    assertThat(actual.workOnRelease!!.notHopingToWorkReasons).containsExactly(NotHopingToWorkReason.OTHER)
    assertThat(actual.workOnRelease!!.notHopingToWorkOtherReason).isEqualTo("No motivation")
    assertThat(actual.workOnRelease!!.affectAbilityToWork).containsExactly(AffectAbilityToWork.OTHER)
    assertThat(actual.workOnRelease!!.affectAbilityToWorkOther).isEqualTo("Negative attitude")
    assertThat(actual.workOnRelease!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.workOnRelease!!.updatedAtPrison).isEqualTo(prisonId)

    assertThat(actual.previousQualifications).isNotNull()
    assertThat(actual.previousQualifications!!.id).isNotNull()
    assertThat(actual.previousQualifications!!.reference).isNotNull()
    assertThat(actual.previousQualifications!!.educationLevel).isEqualTo(HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(actual.previousQualifications!!.qualifications).hasSize(1)
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
    assertThat(actual.previousTraining!!.trainingTypeOther).isEqualTo("Kotlin course")
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

    assertThat(actual.futureWorkInterests).isNotNull()
    assertThat(actual.futureWorkInterests!!.id).isNotNull()
    assertThat(actual.futureWorkInterests!!.reference).isNotNull()
    assertThat(actual.futureWorkInterests!!.interests).hasSize(1)
    assertThat(actual.futureWorkInterests!!.createdAt).isNotNull()
    assertThat(actual.futureWorkInterests!!.createdAtPrison).isEqualTo(prisonId)
    assertThat(actual.futureWorkInterests!!.createdBy).isNotNull()
    assertThat(actual.futureWorkInterests!!.createdByDisplayName).isNotNull()
    assertThat(actual.futureWorkInterests!!.updatedAt).isNotNull()
    assertThat(actual.futureWorkInterests!!.updatedAtPrison).isEqualTo(prisonId)
    assertThat(actual.futureWorkInterests!!.updatedBy).isNotNull()
    assertThat(actual.futureWorkInterests!!.updatedByDisplayName).isNotNull()
  }
}
