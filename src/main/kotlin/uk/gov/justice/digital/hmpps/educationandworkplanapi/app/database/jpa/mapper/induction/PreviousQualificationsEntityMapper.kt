package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    QualificationEntityMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousQualificationsEntityMapper {

  @Autowired
  private lateinit var qualificationEntityMapper: QualificationEntityMapper

  @Autowired
  private lateinit var entityListManager: InductionEntityListManager<QualificationEntity, Qualification>

  @BeanMapping(qualifiedByName = ["addNewQualificationsDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "qualifications", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreatePreviousQualificationsDto): PreviousQualificationsEntity

  @Named("addNewQualificationsDuringCreate")
  @AfterMapping
  fun addNewQualificationsDuringCreate(dto: CreatePreviousQualificationsDto, @MappingTarget entity: PreviousQualificationsEntity) {
    addNewQualifications(dto.qualifications, entity)
  }

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
  abstract fun updateExistingEntityFromDto(
    @MappingTarget entity: PreviousQualificationsEntity,
    dto: UpdatePreviousQualificationsDto?,
  )

  fun updateQualifications(
    entity: PreviousQualificationsEntity,
    dto: UpdatePreviousQualificationsDto,
  ): List<QualificationEntity> {
    val existingQualifications = entity.qualifications!!
    val updatedQualifications = dto.qualifications

    entityListManager.updateExisting(existingQualifications, updatedQualifications, qualificationEntityMapper)
    entityListManager.addNew(entity, existingQualifications, updatedQualifications, qualificationEntityMapper)
    entityListManager.deleteRemoved(existingQualifications, updatedQualifications)

    return existingQualifications
  }

  @BeanMapping(qualifiedByName = ["addNewQualificationsDuringUpdate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "qualifications", ignore = true)
  abstract fun fromUpdateDtoToNewEntity(previousQualifications: UpdatePreviousQualificationsDto?): PreviousQualificationsEntity?

  @Named("addNewQualificationsDuringUpdate")
  @AfterMapping
  fun addNewQualificationsDuringUpdate(dto: UpdatePreviousQualificationsDto, @MappingTarget entity: PreviousQualificationsEntity) {
    addNewQualifications(dto.qualifications, entity)
  }

  private fun addNewQualifications(qualifications: List<Qualification>, entity: PreviousQualificationsEntity) {
    qualifications.forEach {
      entity.addChild(
        qualificationEntityMapper.fromDomainToEntity(it),
        entity.qualifications(),
      )
    }
  }
}

@Mapper
interface QualificationEntityMapper : KeyAwareEntityMapper<QualificationEntity, Qualification> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: Qualification): QualificationEntity

  fun fromEntityToDomain(persistedEntity: QualificationEntity): Qualification

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: QualificationEntity, domain: Qualification)
}
