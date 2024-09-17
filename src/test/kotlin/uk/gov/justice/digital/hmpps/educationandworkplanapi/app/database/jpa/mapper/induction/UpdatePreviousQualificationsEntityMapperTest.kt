package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdateQualificationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidQualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EducationLevel as EducationLevelEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel as QualificationLevelEntity

class UpdatePreviousQualificationsEntityMapperTest {

  private val mapper = PreviousQualificationsEntityMapper(QualificationEntityMapper())

  @Test
  fun `should update existing qualifications`() {
    // Given
    val qualification1Reference = UUID.randomUUID()
    val existingQualificationEntity1 = aValidQualificationEntity(
      reference = qualification1Reference,
      subject = "English",
      level = QualificationLevelEntity.LEVEL_3,
      grade = "A",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    val qualification2Reference = UUID.randomUUID()
    val existingQualificationEntity2 = aValidQualificationEntity(
      reference = qualification2Reference,
      subject = "Maths",
      level = QualificationLevelEntity.LEVEL_2,
      grade = "B",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    val existingQualificationsReference = UUID.randomUUID()
    val existingQualificationsEntity = aValidPreviousQualificationsEntity(
      reference = existingQualificationsReference,
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(existingQualificationEntity1, existingQualificationEntity2),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updatedQualification = aValidUpdateQualificationDto(
      reference = qualification1Reference,
      subject = "English",
      level = QualificationLevelDomain.LEVEL_3,
      grade = "B",
      prisonId = "MDI",
    )
    val unchangedQualification = aValidUpdateQualificationDto(
      reference = qualification2Reference,
      subject = "Maths",
      level = QualificationLevelDomain.LEVEL_2,
      grade = "B",
      prisonId = "MDI",
    )
    val updatedQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = existingQualificationsReference,
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
          subject = "English"
          level = QualificationLevelEntity.LEVEL_3
          grade = "B"
          createdAtPrison = "BXI"
          updatedAtPrison = "MDI"
        },
        // qualification 2 should not have been updated at all because it's subject, level and grade had not changed in the request object
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
    val existingQualificationsReference = UUID.randomUUID()
    val qualification1Reference = UUID.randomUUID()
    val existingQualificationsEntity = aValidPreviousQualificationsEntity(
      reference = existingQualificationsReference,
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(
        aValidQualificationEntity(
          reference = qualification1Reference,
          subject = "English",
          level = QualificationLevelEntity.LEVEL_3,
          grade = "A",
        ),
      ),
    )

    // same level as above, so the grade should be updated
    val updatedQualification = aValidUpdateQualificationDto(
      reference = qualification1Reference,
      subject = "English",
      level = QualificationLevelDomain.LEVEL_3,
      grade = "B",
    )
    // different level, so should appear as a new qualification
    val newQualification = aValidCreateQualificationDto(
      subject = "English",
      level = QualificationLevelDomain.LEVEL_2,
      grade = "C",
    )
    val updatedQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = existingQualificationsReference,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(updatedQualification, newQualification),
      prisonId = "MDI",
    )

    val expectedEntity = existingQualificationsEntity.deepCopy().apply {
      reference = existingQualificationsReference
      educationLevel = EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS
      qualifications = mutableListOf(
        // updated grade
        aValidQualificationEntity(
          reference = qualification1Reference,
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

    val existingQualification = aValidUpdateQualificationDto(
      reference = qualificationReference,
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
      reference = existingQualificationsReference
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
