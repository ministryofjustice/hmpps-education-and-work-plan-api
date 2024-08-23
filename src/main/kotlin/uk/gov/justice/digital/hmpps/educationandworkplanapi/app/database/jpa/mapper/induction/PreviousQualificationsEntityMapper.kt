package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.UpdateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EducationLevel as EducationLevelEntity

@Mapper(
  uses = [
    QualificationEntityMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousQualificationsEntityMapper {

  @Autowired
  private lateinit var qualificationEntityMapper: QualificationEntityMapper

  fun fromCreateDtoToEntity(dto: CreatePreviousQualificationsDto): PreviousQualificationsEntity =
    with(dto) {
      PreviousQualificationsEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
        educationLevel = toEducationLevel(educationLevel),
      ).also { entity ->
        entity.qualifications().addAll(
          qualifications.map {
            qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
          },
        )
      }
    }

  fun updateExistingEntityFromDto(entity: PreviousQualificationsEntity, dto: CreatePreviousQualificationsDto) =
    with(entity) {
      updatedAtPrison = dto.prisonId
      educationLevel = toEducationLevel(dto.educationLevel)

      // Updating the collection of QualificationEntity's within the PreviousQualificationsEntity requires that we do any
      // updates to individual QualificationEntity's first, then delete any that are not explicitly in the update DTO,
      // then finally add any new QualificationEntity's.
      // It is very important that we process the qualifications in this order.
      val existingQualificationsReferences = qualifications().map { qualificationEntity -> qualificationEntity.reference }

      // Update existing qualifications identified by matching reference of DTO to reference of entity
      val dtosRepresentingUpdatesToExistingQualifications = dto.qualifications
        .filterIsInstance<UpdateQualificationDto>()
        .filter { existingQualificationsReferences.contains(it.reference) }
      dtosRepresentingUpdatesToExistingQualifications.onEach { updateQualificationDto ->
        val qualificationEntityToUpdate = qualifications().first { qualificationEntity -> qualificationEntity.reference == updateQualificationDto.reference }
        qualificationEntityMapper.updateEntityFromDomain(qualificationEntityToUpdate, updateQualificationDto)
      }

      // Delete existing qualifications where there is not a corresponding qualification DTO
      qualifications().removeIf { qualificationEntity ->
        !dtosRepresentingUpdatesToExistingQualifications.map { it.reference }.contains(qualificationEntity.reference)
      }

      // Add new QualificationEntity's
      val dtosRepresentingNewQualifications = dto.qualifications
        .filter { it is CreateQualificationDto || !existingQualificationsReferences.contains((it as UpdateQualificationDto).reference) }
      qualifications().addAll(
        dtosRepresentingNewQualifications.map {
          qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
        },
      )
    }

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: PreviousQualificationsEntity?): PreviousQualifications

  fun updateExistingEntityFromDto(entity: PreviousQualificationsEntity, dto: UpdatePreviousQualificationsDto) =
    with(entity) {
      updatedAtPrison = dto.prisonId
      educationLevel = toEducationLevel(dto.educationLevel ?: EducationLevelDomain.NOT_SURE)

      // Updating the collection of QualificationEntity's within the PreviousQualificationsEntity requires that we do any
      // updates to individual QualificationEntity's first, then delete any that are not explicitly in the update DTO,
      // then finally add any new QualificationEntity's.
      // It is very important that we process the qualifications in this order.
      val existingQualificationsReferences = qualifications().map { qualificationEntity -> qualificationEntity.reference }

      // Update existing qualifications identified by matching reference of DTO to reference of entity
      val dtosRepresentingUpdatesToExistingQualifications = dto.qualifications
        .filterIsInstance<UpdateQualificationDto>()
        .filter { existingQualificationsReferences.contains(it.reference) }
      dtosRepresentingUpdatesToExistingQualifications.onEach { updateQualificationDto ->
        val qualificationEntityToUpdate = qualifications().first { qualificationEntity -> qualificationEntity.reference == updateQualificationDto.reference }
        qualificationEntityMapper.updateEntityFromDomain(qualificationEntityToUpdate, updateQualificationDto)
      }

      // Delete existing qualifications where there is not a corresponding qualification DTO
      qualifications().removeIf { qualificationEntity ->
        !dtosRepresentingUpdatesToExistingQualifications.map { it.reference }.contains(qualificationEntity.reference)
      }

      // Add new QualificationEntity's
      val dtosRepresentingNewQualifications = dto.qualifications
        .filter { it is CreateQualificationDto || !existingQualificationsReferences.contains((it as UpdateQualificationDto).reference) }
      qualifications().addAll(
        dtosRepresentingNewQualifications.map {
          qualificationEntityMapper.fromDomainToEntity(it).apply { parent = entity }
        },
      )
    }

  fun toEducationLevel(educationLevel: EducationLevelDomain): EducationLevelEntity =
    when (educationLevel) {
      EducationLevelDomain.NOT_SURE -> EducationLevelEntity.NOT_SURE
      EducationLevelDomain.PRIMARY_SCHOOL -> EducationLevelEntity.PRIMARY_SCHOOL
      EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelEntity.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
      EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelEntity.SECONDARY_SCHOOL_TOOK_EXAMS
      EducationLevelDomain.FURTHER_EDUCATION_COLLEGE -> EducationLevelEntity.FURTHER_EDUCATION_COLLEGE
      EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelEntity.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
      EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelEntity.POSTGRADUATE_DEGREE_AT_UNIVERSITY
    }
}

@Mapper
interface QualificationEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  fun fromDomainToEntity(domain: UpdateOrCreateQualificationDto): QualificationEntity

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  fun updateEntityFromDomain(@MappingTarget entity: QualificationEntity, domain: UpdateOrCreateQualificationDto)

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  fun fromEntityToDomain(persistedEntity: QualificationEntity): Qualification
}
