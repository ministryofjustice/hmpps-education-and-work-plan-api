package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.UpdateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EducationLevel as EducationLevelEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel as QualificationLevelEntity

@Component
class PreviousQualificationsEntityMapper(private val qualificationEntityMapper: QualificationEntityMapper) {

  fun fromCreateDtoToEntity(dto: CreatePreviousQualificationsDto): PreviousQualificationsEntity = with(dto) {
    PreviousQualificationsEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
      educationLevel = toEducationLevel(educationLevel),
    ).also { entity ->
      entity.qualifications.addAll(
        qualifications.map {
          qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
        },
      )
    }
  }

  fun updateExistingEntityFromDto(entity: PreviousQualificationsEntity, dto: CreatePreviousQualificationsDto) = with(entity) {
    updatedAtPrison = dto.prisonId
    educationLevel = toEducationLevel(dto.educationLevel)

    // Updating the collection of QualificationEntity's within the PreviousQualificationsEntity requires that we do any
    // updates to individual QualificationEntity's first, then delete any that are not explicitly in the update DTO,
    // then finally add any new QualificationEntity's.
    // It is very important that we process the qualifications in this order.
    val existingQualificationsReferences = qualifications.map { qualificationEntity -> qualificationEntity.reference }

    // Update existing qualifications identified by matching reference of DTO to reference of entity
    val dtosRepresentingUpdatesToExistingQualifications = dto.qualifications
      .filterIsInstance<UpdateQualificationDto>()
      .filter { existingQualificationsReferences.contains(it.reference) }
    dtosRepresentingUpdatesToExistingQualifications.onEach { updateQualificationDto ->
      val qualificationEntityToUpdate = qualifications.first { qualificationEntity -> qualificationEntity.reference == updateQualificationDto.reference }

      if (qualificationDataHasChanged(qualificationEntityToUpdate, updateQualificationDto)) {
        qualificationEntityMapper.updateEntityFromDomain(qualificationEntityToUpdate, updateQualificationDto)
      }
    }

    // Delete existing qualifications where there is not a corresponding qualification DTO
    qualifications.removeIf { qualificationEntity ->
      !dtosRepresentingUpdatesToExistingQualifications.map { it.reference }.contains(qualificationEntity.reference)
    }

    // Add new QualificationEntity's
    val dtosRepresentingNewQualifications = dto.qualifications
      .filter { it is CreateQualificationDto || !existingQualificationsReferences.contains((it as UpdateQualificationDto).reference) }
    qualifications.addAll(
      dtosRepresentingNewQualifications.map {
        qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
      },
    )
  }

  fun fromEntityToDomain(persistedEntity: PreviousQualificationsEntity): PreviousQualifications = with(persistedEntity) {
    PreviousQualifications(
      reference = reference,
      prisonNumber = prisonNumber,
      educationLevel = toEducationLevel(educationLevel),
      qualifications = qualifications.map { qualificationEntityMapper.fromEntityToDomain(it) },
      createdBy = createdBy!!,
      createdAt = createdAt!!,
      createdAtPrison = createdAtPrison,
      lastUpdatedBy = updatedBy!!,
      lastUpdatedAt = updatedAt!!,
      lastUpdatedAtPrison = updatedAtPrison,
    )
  }

  fun updateExistingEntityFromDto(entity: PreviousQualificationsEntity, dto: UpdatePreviousQualificationsDto) = with(entity) {
    updatedAtPrison = dto.prisonId
    educationLevel = toEducationLevel(dto.educationLevel ?: EducationLevelDomain.NOT_SURE)

    // Updating the collection of QualificationEntity's within the PreviousQualificationsEntity requires that we do any
    // updates to individual QualificationEntity's first, then delete any that are not explicitly in the update DTO,
    // then finally add any new QualificationEntity's.
    // It is very important that we process the qualifications in this order.
    val existingQualificationsReferences = qualifications.map { qualificationEntity -> qualificationEntity.reference }

    // Update existing qualifications identified by matching reference of DTO to reference of entity
    val dtosRepresentingUpdatesToExistingQualifications = dto.qualifications
      .filterIsInstance<UpdateQualificationDto>()
      .filter { existingQualificationsReferences.contains(it.reference) }
    dtosRepresentingUpdatesToExistingQualifications.onEach { updateQualificationDto ->
      val qualificationEntityToUpdate = qualifications.first { qualificationEntity -> qualificationEntity.reference == updateQualificationDto.reference }

      if (qualificationDataHasChanged(qualificationEntityToUpdate, updateQualificationDto)) {
        qualificationEntityMapper.updateEntityFromDomain(qualificationEntityToUpdate, updateQualificationDto)
      }
    }

    // Delete existing qualifications where there is not a corresponding qualification DTO
    qualifications.removeIf { qualificationEntity ->
      !dtosRepresentingUpdatesToExistingQualifications.map { it.reference }.contains(qualificationEntity.reference)
    }

    // Add new QualificationEntity's
    val dtosRepresentingNewQualifications = dto.qualifications
      .filter { it is CreateQualificationDto || !existingQualificationsReferences.contains((it as UpdateQualificationDto).reference) }
    qualifications.addAll(
      dtosRepresentingNewQualifications.map {
        qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
      },
    )
  }

  private fun qualificationDataHasChanged(qualificationEntityToUpdate: QualificationEntity, updateQualificationDto: UpdateQualificationDto): Boolean = with(qualificationEntityToUpdate) {
    !(subject == updateQualificationDto.subject && grade == updateQualificationDto.grade && toQualificationLevel(level) == updateQualificationDto.level)
  }
}

@Component
class QualificationEntityMapper {
  fun fromDomainToEntity(domain: UpdateOrCreateQualificationDto): QualificationEntity = QualificationEntity(
    reference = UUID.randomUUID(),
    subject = domain.subject,
    grade = domain.grade,
    level = toQualificationLevel(domain.level),
    createdAtPrison = domain.prisonId,
    updatedAtPrison = domain.prisonId,
  )

  fun updateEntityFromDomain(entity: QualificationEntity, domain: UpdateOrCreateQualificationDto) = with(entity) {
    subject = domain.subject
    grade = domain.grade
    level = toQualificationLevel(domain.level)
    updatedAtPrison = domain.prisonId
  }

  fun fromEntityToDomain(persistedEntity: QualificationEntity): Qualification = with(persistedEntity) {
    Qualification(
      reference = reference,
      subject = subject,
      level = toQualificationLevel(level),
      grade = grade,
      createdBy = createdBy!!,
      createdAtPrison = createdAtPrison,
      createdAt = createdAt!!,
      lastUpdatedBy = updatedBy!!,
      lastUpdatedAt = updatedAt!!,
      lastUpdatedAtPrison = updatedAtPrison!!,
    )
  }
}

private fun toQualificationLevel(qualificationLevel: QualificationLevelEntity): QualificationLevelDomain = when (qualificationLevel) {
  QualificationLevelEntity.ENTRY_LEVEL -> QualificationLevelDomain.ENTRY_LEVEL
  QualificationLevelEntity.LEVEL_1 -> QualificationLevelDomain.LEVEL_1
  QualificationLevelEntity.LEVEL_2 -> QualificationLevelDomain.LEVEL_2
  QualificationLevelEntity.LEVEL_3 -> QualificationLevelDomain.LEVEL_3
  QualificationLevelEntity.LEVEL_4 -> QualificationLevelDomain.LEVEL_4
  QualificationLevelEntity.LEVEL_5 -> QualificationLevelDomain.LEVEL_5
  QualificationLevelEntity.LEVEL_6 -> QualificationLevelDomain.LEVEL_6
  QualificationLevelEntity.LEVEL_7 -> QualificationLevelDomain.LEVEL_7
  QualificationLevelEntity.LEVEL_8 -> QualificationLevelDomain.LEVEL_8
}

private fun toQualificationLevel(qualificationLevel: QualificationLevelDomain): QualificationLevelEntity = when (qualificationLevel) {
  QualificationLevelDomain.ENTRY_LEVEL -> QualificationLevelEntity.ENTRY_LEVEL
  QualificationLevelDomain.LEVEL_1 -> QualificationLevelEntity.LEVEL_1
  QualificationLevelDomain.LEVEL_2 -> QualificationLevelEntity.LEVEL_2
  QualificationLevelDomain.LEVEL_3 -> QualificationLevelEntity.LEVEL_3
  QualificationLevelDomain.LEVEL_4 -> QualificationLevelEntity.LEVEL_4
  QualificationLevelDomain.LEVEL_5 -> QualificationLevelEntity.LEVEL_5
  QualificationLevelDomain.LEVEL_6 -> QualificationLevelEntity.LEVEL_6
  QualificationLevelDomain.LEVEL_7 -> QualificationLevelEntity.LEVEL_7
  QualificationLevelDomain.LEVEL_8 -> QualificationLevelEntity.LEVEL_8
}

private fun toEducationLevel(educationLevel: EducationLevelDomain): EducationLevelEntity = when (educationLevel) {
  EducationLevelDomain.NOT_SURE -> EducationLevelEntity.NOT_SURE
  EducationLevelDomain.NO_FORMAL_EDUCATION -> EducationLevelEntity.NO_FORMAL_EDUCATION
  EducationLevelDomain.PRIMARY_SCHOOL -> EducationLevelEntity.PRIMARY_SCHOOL
  EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelEntity.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
  EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS
  EducationLevelDomain.FURTHER_EDUCATION_COLLEGE -> EducationLevelEntity.FURTHER_EDUCATION_COLLEGE
  EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelEntity.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
  EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelEntity.POSTGRADUATE_DEGREE_AT_UNIVERSITY
}

private fun toEducationLevel(educationLevel: EducationLevelEntity): EducationLevelDomain = when (educationLevel) {
  EducationLevelEntity.NOT_SURE -> EducationLevelDomain.NOT_SURE
  EducationLevelEntity.NO_FORMAL_EDUCATION -> EducationLevelDomain.NO_FORMAL_EDUCATION
  EducationLevelEntity.PRIMARY_SCHOOL -> EducationLevelDomain.PRIMARY_SCHOOL
  EducationLevelEntity.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
  EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS
  EducationLevelEntity.FURTHER_EDUCATION_COLLEGE -> EducationLevelDomain.FURTHER_EDUCATION_COLLEGE
  EducationLevelEntity.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
  EducationLevelEntity.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY
}
