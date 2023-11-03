package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInPrisonInterestsDto

@Mapper(
  uses = [
    InPrisonWorkInterestEntityMapper::class,
    InPrisonTrainingInterestEntityMapper::class,
  ],
)
abstract class InPrisonInterestsEntityMapper {

  @Autowired
  private lateinit var inPrisonWorkInterestEntityMapper: InPrisonWorkInterestEntityMapper

  @Autowired
  private lateinit var inPrisonTrainingInterestEntityMapper: InPrisonTrainingInterestEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreateInPrisonInterestsDto): InPrisonInterestsEntity

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
  abstract fun updateEntityFromDto(@MappingTarget entity: InPrisonInterestsEntity, dto: UpdateInPrisonInterestsDto)

  fun updateWorkInterests(
    entity: InPrisonInterestsEntity,
    dto: UpdateInPrisonInterestsDto,
  ): List<InPrisonWorkInterestEntity> {
    val existingInterests = entity.inPrisonWorkInterests!!
    val updatedInterests = dto.inPrisonWorkInterests

    updateExistingWorkInterests(existingInterests, updatedInterests)
    addNewWorkInterests(existingInterests, updatedInterests)
    removeWorkInterests(existingInterests, updatedInterests)

    return existingInterests
  }

  fun updateTrainingInterests(
    entity: InPrisonInterestsEntity,
    dto: UpdateInPrisonInterestsDto,
  ): List<InPrisonTrainingInterestEntity> {
    val existingInterests = entity.inPrisonTrainingInterests!!
    val updatedInterests = dto.inPrisonTrainingInterests

    updateExistingTrainingInterests(existingInterests, updatedInterests)
    addNewTrainingInterests(existingInterests, updatedInterests)
    removeTrainingInterests(existingInterests, updatedInterests)

    return existingInterests
  }

  private fun updateExistingWorkInterests(
    existingInterests: MutableList<InPrisonWorkInterestEntity>,
    updatedInterests: List<InPrisonWorkInterest>,
  ) {
    val updatedWorkTypes = updatedInterests.map { it.workType.name }
    existingInterests
      .filter { interestEntity -> updatedWorkTypes.contains(interestEntity.workType!!.name) }
      .onEach { interestEntity ->
        inPrisonWorkInterestEntityMapper.updateEntityFromDomain(
          interestEntity,
          updatedInterests.first { updatedInterestDomain -> updatedInterestDomain.workType.name == interestEntity.workType!!.name },
        )
      }
  }

  private fun addNewWorkInterests(
    existingInterests: MutableList<InPrisonWorkInterestEntity>,
    updatedInterests: List<InPrisonWorkInterest>,
  ) {
    val currentWorkInterestTypes = existingInterests.map { it.workType!!.name }
    existingInterests.addAll(
      updatedInterests
        .filter { updatedInterestDto -> !currentWorkInterestTypes.contains(updatedInterestDto.workType.name) }
        .map { newInterestDto -> inPrisonWorkInterestEntityMapper.fromDomainToEntity(newInterestDto) },
    )
  }

  private fun removeWorkInterests(
    existingInterests: MutableList<InPrisonWorkInterestEntity>,
    updatedInterests: List<InPrisonWorkInterest>,
  ) {
    val updatedInterestTypes = updatedInterests.map { it.workType.name }
    existingInterests.removeIf { interestEntity ->
      !updatedInterestTypes.contains(interestEntity.workType!!.name)
    }
  }

  private fun updateExistingTrainingInterests(
    existingInterests: MutableList<InPrisonTrainingInterestEntity>,
    updatedInterests: List<InPrisonTrainingInterest>,
  ) {
    val updatedTrainingTypes = updatedInterests.map { it.trainingType.name }
    existingInterests
      .filter { interestEntity -> updatedTrainingTypes.contains(interestEntity.trainingType!!.name) }
      .onEach { interestEntity ->
        inPrisonTrainingInterestEntityMapper.updateEntityFromDomain(
          interestEntity,
          updatedInterests.first { updatedInterestDomain -> updatedInterestDomain.trainingType.name == interestEntity.trainingType!!.name },
        )
      }
  }

  private fun addNewTrainingInterests(
    existingInterests: MutableList<InPrisonTrainingInterestEntity>,
    updatedInterests: List<InPrisonTrainingInterest>,
  ) {
    val currentTrainingInterestTypes = existingInterests.map { it.trainingType!!.name }
    existingInterests.addAll(
      updatedInterests
        .filter { updatedInterestDto -> !currentTrainingInterestTypes.contains(updatedInterestDto.trainingType.name) }
        .map { newInterestDto -> inPrisonTrainingInterestEntityMapper.fromDomainToEntity(newInterestDto) },
    )
  }

  private fun removeTrainingInterests(
    existingInterests: MutableList<InPrisonTrainingInterestEntity>,
    updatedInterests: List<InPrisonTrainingInterest>,
  ) {
    val updatedInterestTypes = updatedInterests.map { it.trainingType.name }
    existingInterests.removeIf { interestEntity ->
      !updatedInterestTypes.contains(interestEntity.trainingType!!.name)
    }
  }
}

@Mapper
interface InPrisonWorkInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: InPrisonWorkInterest): InPrisonWorkInterestEntity

  fun fromEntityToDomain(persistedEntity: InPrisonWorkInterestEntity): InPrisonWorkInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: InPrisonWorkInterestEntity, domain: InPrisonWorkInterest)
}

@Mapper
interface InPrisonTrainingInterestEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: InPrisonTrainingInterest): InPrisonTrainingInterestEntity

  fun fromEntityToDomain(persistedEntity: InPrisonTrainingInterestEntity): InPrisonTrainingInterest

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: InPrisonTrainingInterestEntity, domain: InPrisonTrainingInterest)
}
