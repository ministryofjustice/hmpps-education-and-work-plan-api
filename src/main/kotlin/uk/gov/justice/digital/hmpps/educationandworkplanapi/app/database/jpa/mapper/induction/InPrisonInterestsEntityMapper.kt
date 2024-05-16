package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    InPrisonWorkInterestEntityMapper::class,
    InPrisonTrainingInterestEntityMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class InPrisonInterestsEntityMapper {

  @Autowired
  private lateinit var workInterestEntityMapper: InPrisonWorkInterestEntityMapper

  @Autowired
  private lateinit var trainingInterestEntityMapper: InPrisonTrainingInterestEntityMapper

  @Autowired
  private lateinit var workInterestEntityListManager: InductionEntityListManager<InPrisonWorkInterestEntity, InPrisonWorkInterest>

  @Autowired
  private lateinit var trainingInterestEntityListManager: InductionEntityListManager<InPrisonTrainingInterestEntity, InPrisonTrainingInterest>

  @BeanMapping(qualifiedByName = ["addNewInterestsDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "inPrisonWorkInterests", ignore = true)
  @Mapping(target = "inPrisonTrainingInterests", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreateInPrisonInterestsDto): InPrisonInterestsEntity

  @Named("addNewInterestsDuringCreate")
  @AfterMapping
  fun addNewInterestsDuringCreate(dto: CreateInPrisonInterestsDto, @MappingTarget entity: InPrisonInterestsEntity) {
    addNewWorkInterests(dto.inPrisonWorkInterests, entity)
    addNewTrainingInterests(dto.inPrisonTrainingInterests, entity)
  }

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: InPrisonInterestsEntity): InPrisonInterests

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "inPrisonWorkInterests", expression = "java( updateWorkInterests(entity, dto) )")
  @Mapping(target = "inPrisonTrainingInterests", expression = "java( updateTrainingInterests(entity, dto) )")
  abstract fun updateExistingEntityFromDto(@MappingTarget entity: InPrisonInterestsEntity, dto: UpdateInPrisonInterestsDto?)

  fun updateWorkInterests(
    entity: InPrisonInterestsEntity,
    dto: UpdateInPrisonInterestsDto,
  ): List<InPrisonWorkInterestEntity> {
    val existingInterests = entity.inPrisonWorkInterests!!
    val updatedInterests = dto.inPrisonWorkInterests

    workInterestEntityListManager.updateExisting(existingInterests, updatedInterests, workInterestEntityMapper)
    workInterestEntityListManager.addNew(entity, existingInterests, updatedInterests, workInterestEntityMapper)
    workInterestEntityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
  }

  fun updateTrainingInterests(
    entity: InPrisonInterestsEntity,
    dto: UpdateInPrisonInterestsDto,
  ): List<InPrisonTrainingInterestEntity> {
    val existingInterests = entity.inPrisonTrainingInterests!!
    val updatedInterests = dto.inPrisonTrainingInterests

    trainingInterestEntityListManager.updateExisting(existingInterests, updatedInterests, trainingInterestEntityMapper)
    trainingInterestEntityListManager.addNew(entity, existingInterests, updatedInterests, trainingInterestEntityMapper)
    trainingInterestEntityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
  }

  @BeanMapping(qualifiedByName = ["addNewInterestsDuringUpdate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "inPrisonWorkInterests", ignore = true)
  @Mapping(target = "inPrisonTrainingInterests", ignore = true)
  abstract fun fromUpdateDtoToNewEntity(inPrisonInterests: UpdateInPrisonInterestsDto?): InPrisonInterestsEntity?

  @Named("addNewInterestsDuringUpdate")
  @AfterMapping
  fun addNewInterestsDuringUpdate(dto: UpdateInPrisonInterestsDto, @MappingTarget entity: InPrisonInterestsEntity) {
    addNewWorkInterests(dto.inPrisonWorkInterests, entity)
    addNewTrainingInterests(dto.inPrisonTrainingInterests, entity)
  }

  private fun addNewWorkInterests(workInterests: List<InPrisonWorkInterest>, entity: InPrisonInterestsEntity) {
    workInterests.forEach {
      entity.addChild(
        workInterestEntityMapper.fromDomainToEntity(it),
        entity.inPrisonWorkInterests(),
      )
    }
  }

  private fun addNewTrainingInterests(workInterests: List<InPrisonTrainingInterest>, entity: InPrisonInterestsEntity) {
    workInterests.forEach {
      entity.addChild(
        trainingInterestEntityMapper.fromDomainToEntity(it),
        entity.inPrisonTrainingInterests(),
      )
    }
  }
}

@Mapper
interface InPrisonWorkInterestEntityMapper : KeyAwareEntityMapper<InPrisonWorkInterestEntity, InPrisonWorkInterest> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: InPrisonWorkInterest): InPrisonWorkInterestEntity

  fun fromEntityToDomain(persistedEntity: InPrisonWorkInterestEntity): InPrisonWorkInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: InPrisonWorkInterestEntity, domain: InPrisonWorkInterest)
}

@Mapper
interface InPrisonTrainingInterestEntityMapper :
  KeyAwareEntityMapper<InPrisonTrainingInterestEntity, InPrisonTrainingInterest> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: InPrisonTrainingInterest): InPrisonTrainingInterestEntity

  fun fromEntityToDomain(persistedEntity: InPrisonTrainingInterestEntity): InPrisonTrainingInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(
    @MappingTarget entity: InPrisonTrainingInterestEntity,
    domain: InPrisonTrainingInterest,
  )
}
