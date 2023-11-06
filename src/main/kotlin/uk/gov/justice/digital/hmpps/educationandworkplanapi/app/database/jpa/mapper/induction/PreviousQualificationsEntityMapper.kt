package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousQualificationsDto

@Mapper(
  uses = [
    QualificationEntityMapper::class,
  ],
)
abstract class PreviousQualificationsEntityMapper {

  @Autowired
  private lateinit var qualificationEntityMapper: QualificationEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreatePreviousQualificationsDto): PreviousQualificationsEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: PreviousQualificationsEntity): PreviousQualifications

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "qualifications", expression = "java( updateQualifications(entity, dto) )")
  abstract fun updateEntityFromDto(
    @MappingTarget entity: PreviousQualificationsEntity?,
    dto: UpdatePreviousQualificationsDto?,
  )

  fun updateQualifications(
    entity: PreviousQualificationsEntity,
    dto: UpdatePreviousQualificationsDto,
  ): List<QualificationEntity> {
    val existingQualifications = entity.qualifications!!
    val updatedQualifications = dto.qualifications

    updateExistingQualifications(existingQualifications, updatedQualifications)
    addNewQualifications(existingQualifications, updatedQualifications)
    removeQualifications(existingQualifications, updatedQualifications)

    return existingQualifications
  }

  private fun updateExistingQualifications(
    existingQualifications: MutableList<QualificationEntity>,
    updatedQualifications: List<Qualification>,
  ) {
    val updatedSubjects = updatedQualifications.map { it.subject }
    existingQualifications
      .filter { qualificationEntity -> updatedSubjects.contains(qualificationEntity.subject) }
      .onEach { qualificationEntity ->
        qualificationEntityMapper.updateEntityFromDomain(
          qualificationEntity,
          updatedQualifications.first { updatedQualificationDto -> updatedQualificationDto.subject == qualificationEntity.subject },
        )
      }
  }

  private fun addNewQualifications(
    existingQualifications: MutableList<QualificationEntity>,
    updatedQualifications: List<Qualification>,
  ) {
    val existingSubjects = existingQualifications.map { it.subject }
    existingQualifications.addAll(
      updatedQualifications
        .filter { updatedQualification -> !existingSubjects.contains(updatedQualification.subject) }
        .map { newQualification -> qualificationEntityMapper.fromDomainToEntity(newQualification) },
    )
  }

  private fun removeQualifications(
    existingQualifications: MutableList<QualificationEntity>,
    updatedQualifications: List<Qualification>,
  ) {
    val updatedSubjects = updatedQualifications.map { it.subject }
    existingQualifications.removeIf { qualificationEntity ->
      !updatedSubjects.contains(qualificationEntity.subject)
    }
  }
}

@Mapper
interface QualificationEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: Qualification): QualificationEntity

  fun fromEntityToDomain(persistedEntity: QualificationEntity): Qualification

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: QualificationEntity, domain: Qualification)
}
