package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidQualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidQualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EducationLevel as EducationLevelEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel as QualificationLevelEntity

class UpdatePreviousQualificationsEntityMapperTest {

  private val mapper = PreviousQualificationsEntityMapperImpl().also {
    PreviousQualificationsEntityMapper::class.java.getDeclaredField("qualificationEntityMapper").apply {
      isAccessible = true
      set(it, QualificationEntityMapperImpl())
    }

    PreviousQualificationsEntityMapper::class.java.getDeclaredField("entityListManager").apply {
      isAccessible = true
      set(it, InductionEntityListManager<QualificationEntity, Qualification>())
    }
  }

  @Test
  fun `should update existing qualifications`() {
    // Given
    val qualificationReference = UUID.randomUUID()
    val existingQualificationEntity1 = aValidQualificationEntity(
      reference = qualificationReference,
      subject = "English",
      level = QualificationLevelEntity.LEVEL_3,
      grade = "A",
    )
    val existingQualificationEntity2 = aValidQualificationEntity(
      reference = qualificationReference,
      subject = "Maths",
      level = QualificationLevelEntity.LEVEL_2,
      grade = "B",
    )
    val existingQualificationsReference = UUID.randomUUID()
    val existingQualificationsEntity = aValidPreviousQualificationsEntity(
      reference = existingQualificationsReference,
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(existingQualificationEntity1, existingQualificationEntity2),
    )

    // ensure case is ignored
    val updatedQualification = aValidQualification(
      subject = "ENGLISH",
      level = QualificationLevelDomain.LEVEL_3,
      grade = "B",
    )
    val unchangedQualification = aValidQualification(
      subject = "Maths",
      level = QualificationLevelDomain.LEVEL_2,
      grade = "B",
    )
    val updatedQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = qualificationReference,
      educationLevel = EducationLevelDomain.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(updatedQualification, unchangedQualification),
      prisonId = "MDI",
    )

    val expectedEntity = existingQualificationsEntity.deepCopy().apply {
      id
      reference = reference
      educationLevel = EducationLevelEntity.FURTHER_EDUCATION_COLLEGE
      qualifications = mutableListOf(
        existingQualificationEntity1.deepCopy().apply {
          subject = "ENGLISH"
          level = QualificationLevelEntity.LEVEL_3
          grade = "B"
        },
        existingQualificationEntity2.deepCopy(),
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingQualificationsEntity, updatedQualificationsDto)

    // Then
    assertThat(existingQualificationsEntity).isEqualToComparingAllFields(expectedEntity)
  }

  @Test
  fun `should update existing qualification and add new qualification`() {
    // Given
    val qualificationReference = UUID.randomUUID()
    val existingQualificationsEntity = aValidPreviousQualificationsEntity(
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(
        aValidQualificationEntity(
          reference = qualificationReference,
          subject = "English",
          level = QualificationLevelEntity.LEVEL_3,
          grade = "A",
        ),
      ),
    )

    // same level as above, so the grade should be updated
    val updatedQualification = aValidQualification(
      subject = "English",
      level = QualificationLevelDomain.LEVEL_3,
      grade = "B",
    )
    // different level, so should appear as a new qualification
    val newQualification = aValidQualification(
      subject = "English",
      level = QualificationLevelDomain.LEVEL_2,
      grade = "C",
    )
    val updatedQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = qualificationReference,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(updatedQualification, newQualification),
      prisonId = "MDI",
    )

    val expectedEntity = existingQualificationsEntity.deepCopy().apply {
      id
      reference = reference
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS
      qualifications = mutableListOf(
        // updated grade
        aValidQualificationEntity(
          subject = "English",
          level = QualificationLevelEntity.LEVEL_3,
          grade = "B",
        ),
        // new qualification
        aValidQualificationEntity(
          subject = "English",
          level = QualificationLevelEntity.LEVEL_2,
          grade = "C",
        ),
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingQualificationsEntity, updatedQualificationsDto)

    // Then
    assertThat(existingQualificationsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove qualification`() {
    // Given
    val qualificationReference = UUID.randomUUID()
    val firstQualificationEntity = aValidQualificationEntity(
      reference = qualificationReference,
      subject = "English",
      level = QualificationLevelEntity.LEVEL_3,
      grade = "A",
    )
    val secondQualificationEntity = aValidQualificationEntity(
      subject = "Maths",
      level = QualificationLevelEntity.LEVEL_2,
      grade = "B",
    )
    val existingQualificationsReference = UUID.randomUUID()
    val existingQualificationsEntity = aValidPreviousQualificationsEntity(
      reference = existingQualificationsReference,
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(firstQualificationEntity, secondQualificationEntity),
    )

    val existingQualification = aValidQualification(
      subject = "English",
      level = QualificationLevelDomain.LEVEL_3,
      grade = "A",
    )
    val updatedQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = qualificationReference,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(existingQualification),
      prisonId = "MDI",
    )

    val expectedEntity = existingQualificationsEntity.deepCopy().apply {
      id
      reference = reference
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS
      qualifications = mutableListOf(firstQualificationEntity)
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingQualificationsEntity, updatedQualificationsDto)

    // Then
    assertThat(existingQualificationsEntity).isEqualToComparingAllFields(expectedEntity)
  }
}
