package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateFutureWorkInterestsDto

@Mapper(
  uses = [
    WorkInterestEntityMapper::class,
  ],
)
abstract class FutureWorkInterestsEntityMapper {
  @Autowired
  private lateinit var workInterestEntityMapper: WorkInterestEntityMapper

  @Autowired
  private lateinit var entityListManager: InductionEntityListManager<WorkInterestEntity, WorkInterest>

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "interests", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreateFutureWorkInterestsDto): FutureWorkInterestsEntity

  @AfterMapping
  fun addInterests(dto: CreateFutureWorkInterestsDto, @MappingTarget entity: FutureWorkInterestsEntity) {
    dto.interests.forEach {
      entity.addChild(
        workInterestEntityMapper.fromDomainToEntity(it),
        entity.interests(),
      )
    }
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
  abstract fun updateEntityFromDto(
    @MappingTarget entity: FutureWorkInterestsEntity?,
    dto: UpdateFutureWorkInterestsDto?,
  )

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "interests", ignore = true)
  abstract fun fromUpdateDtoToEntity(dto: UpdateFutureWorkInterestsDto?): FutureWorkInterestsEntity

  @AfterMapping
  fun addInterests(dto: UpdateFutureWorkInterestsDto, @MappingTarget entity: FutureWorkInterestsEntity) {
    val existingTypes = entity.interests?.map { it.key() }
    dto.interests.forEach {
      // ensure we only add new interests
      if (existingTypes == null || !existingTypes.contains(it.key())) {
        entity.addChild(workInterestEntityMapper.fromDomainToEntity(it), entity.interests())
      }
    }
  }

  fun updateInterests(entity: FutureWorkInterestsEntity, dto: UpdateFutureWorkInterestsDto): List<WorkInterestEntity> {
    val existingInterests = entity.interests!!
    val updatedInterests = dto.interests

    entityListManager.updateExisting(existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.addNew(entity, existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.deleteRemoved(existingInterests, updatedInterests)

    return existingInterests
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
