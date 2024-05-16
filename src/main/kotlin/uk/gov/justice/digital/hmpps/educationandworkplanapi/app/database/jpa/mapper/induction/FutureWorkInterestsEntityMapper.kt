package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    WorkInterestEntityMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class FutureWorkInterestsEntityMapper {
  @Autowired
  private lateinit var workInterestEntityMapper: WorkInterestEntityMapper

  @Autowired
  private lateinit var entityListManager: InductionEntityListManager<WorkInterestEntity, WorkInterest>

  @BeanMapping(qualifiedByName = ["addNewInterestsDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "interests", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreateFutureWorkInterestsDto): FutureWorkInterestsEntity

  @Named("addNewInterestsDuringCreate")
  @AfterMapping
  fun addNewInterestsDuringCreate(dto: CreateFutureWorkInterestsDto, @MappingTarget entity: FutureWorkInterestsEntity) {
    addNewInterests(dto.interests, entity)
  }

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(entity: FutureWorkInterestsEntity): FutureWorkInterests

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "interests", expression = "java( updateInterests(entity, dto) )")
  abstract fun updateExistingEntityFromDto(
    @MappingTarget entity: FutureWorkInterestsEntity,
    dto: UpdateFutureWorkInterestsDto?,
  )

  fun updateInterests(entity: FutureWorkInterestsEntity, dto: UpdateFutureWorkInterestsDto): List<WorkInterestEntity> {
    val existingInterests = entity.interests!!
    val updatedInterests = dto.interests

    entityListManager.updateExisting(existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.addNew(entity, existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
  }

  @BeanMapping(qualifiedByName = ["addNewInterestsDuringUpdate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "interests", ignore = true)
  abstract fun fromUpdateDtoToNewEntity(dto: UpdateFutureWorkInterestsDto?): FutureWorkInterestsEntity

  @Named("addNewInterestsDuringUpdate")
  @AfterMapping
  fun addNewInterestsDuringUpdate(dto: UpdateFutureWorkInterestsDto, @MappingTarget entity: FutureWorkInterestsEntity) {
    addNewInterests(dto.interests, entity)
  }

  private fun addNewInterests(interests: List<WorkInterest>, entity: FutureWorkInterestsEntity) {
    interests.forEach {
      entity.addChild(
        workInterestEntityMapper.fromDomainToEntity(it),
        entity.interests(),
      )
    }
  }
}

@Mapper
interface WorkInterestEntityMapper : KeyAwareEntityMapper<WorkInterestEntity, WorkInterest> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: WorkInterest): WorkInterestEntity

  fun fromEntityToDomain(entity: WorkInterestEntity): WorkInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: WorkInterestEntity, domain: WorkInterest)
}
