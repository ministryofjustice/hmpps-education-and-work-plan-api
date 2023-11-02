package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
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

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreateFutureWorkInterestsDto): FutureWorkInterestsEntity

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
  abstract fun updateEntityFromDto(@MappingTarget entity: FutureWorkInterestsEntity, dto: UpdateFutureWorkInterestsDto)

  fun updateInterests(entity: FutureWorkInterestsEntity, dto: UpdateFutureWorkInterestsDto): List<WorkInterestEntity> {
    val existingInterests = entity.interests!!
    val updatedInterests = dto.interests

    updateExistingInterests(existingInterests, updatedInterests)
    addNewInterests(existingInterests, updatedInterests)
    removeInterests(existingInterests, updatedInterests)

    return existingInterests
  }

  /**
   * Update the [WorkInterestEntity] whose work type matches the corresponding [WorkInterest].
   */
  private fun updateExistingInterests(
    existingInterests: MutableList<WorkInterestEntity>,
    updatedInterests: List<WorkInterest>,
  ) {
    val updatedWorkTypes = updatedInterests.map { it.workType.name }
    existingInterests
      .filter { interestEntity -> updatedWorkTypes.contains(interestEntity.workType!!.name) }
      .onEach { interestEntity ->
        workInterestEntityMapper.updateEntityFromDomain(
          interestEntity,
          updatedInterests.first { updatedInterestDto -> updatedInterestDto.workType.name == interestEntity.workType!!.name },
        )
      }
  }

  /**
   * Add new [WorkInterestEntity]s from the list of updated [WorkInterest]s where the work type is not present in the list of [WorkInterestEntity]s.
   */
  private fun addNewInterests(
    existingInterests: MutableList<WorkInterestEntity>,
    updatedInterests: List<WorkInterest>,
  ) {
    val currentWorkInterestTypes = existingInterests.map { it.workType!!.name }
    existingInterests.addAll(
      updatedInterests
        .filter { updatedInterestDto -> !currentWorkInterestTypes.contains(updatedInterestDto.workType.name) }
        .map { newInterestDto -> workInterestEntityMapper.fromDomainToEntity(newInterestDto) },
    )
  }

  /**
   * Remove any [WorkInterestEntity]s whose work type is not in the list of updated [WorkInterest]s.
   */
  private fun removeInterests(
    existingInterests: MutableList<WorkInterestEntity>,
    updatedInterests: List<WorkInterest>,
  ) {
    val updatedInterestTypes = updatedInterests.map { it.workType.name }
    existingInterests.removeIf { interestEntity ->
      !updatedInterestTypes.contains(interestEntity.workType!!.name)
    }
  }
}

@Mapper
interface WorkInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: WorkInterest): WorkInterestEntity

  fun fromEntityToDomain(entity: WorkInterestEntity): WorkInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: WorkInterestEntity, domain: WorkInterest)
}
