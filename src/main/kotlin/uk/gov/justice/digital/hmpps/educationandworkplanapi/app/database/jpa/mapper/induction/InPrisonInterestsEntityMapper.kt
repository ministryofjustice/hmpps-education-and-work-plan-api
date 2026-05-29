package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkInterestEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkType as InPrisonWorkTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingType as InPrisonTrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkType as InPrisonWorkTypeEntity

@Component
class InPrisonInterestsEntityMapper(
  private val workInterestEntityMapper: InPrisonWorkInterestEntityMapper,
  private val trainingInterestEntityMapper: InPrisonTrainingInterestEntityMapper,
) {

  fun fromCreateDtoToEntity(dto: CreateInPrisonInterestsDto): InPrisonInterestsEntity = with(dto) {
    InPrisonInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewWorkInterests(dto.inPrisonWorkInterests, this)
      addNewTrainingInterests(dto.inPrisonTrainingInterests, this)
    }
  }

  fun fromEntityToDomain(persistedEntity: InPrisonInterestsEntity?): InPrisonInterests? = persistedEntity?.let {
    InPrisonInterests(
      reference = it.reference,
      inPrisonWorkInterests = it.inPrisonWorkInterests.map { workInterestEntityMapper.fromEntityToDomain(it) },
      inPrisonTrainingInterests = it.inPrisonTrainingInterests.map { trainingInterestEntityMapper.fromEntityToDomain(it) },
      createdBy = it.createdBy!!,
      createdAt = it.createdAt!!,
      createdAtPrison = it.createdAtPrison,
      lastUpdatedBy = it.updatedBy!!,
      lastUpdatedAt = it.updatedAt!!,
      lastUpdatedAtPrison = it.updatedAtPrison,
    )
  }

  fun updateExistingEntityFromDto(entity: InPrisonInterestsEntity, dto: UpdateInPrisonInterestsDto) = with(entity) {
    updatedAtPrison = dto.prisonId

    val existingWorkInterests = entity.inPrisonWorkInterests
    val updatedWorkInterests = dto.inPrisonWorkInterests
    updateExistingWorkInterests(existingWorkInterests, updatedWorkInterests)
    addNewWorkInterests(entity, existingWorkInterests, updatedWorkInterests)
    deleteRemovedWorkInterests(existingWorkInterests, updatedWorkInterests)

    val existingTrainingInterests = entity.inPrisonTrainingInterests
    val updatedTrainingInterests = dto.inPrisonTrainingInterests
    updateExistingTrainingInterests(existingTrainingInterests, updatedTrainingInterests)
    addNewTrainingInterests(entity, existingTrainingInterests, updatedTrainingInterests)
    deleteRemovedTrainingInterests(existingTrainingInterests, updatedTrainingInterests)
  }

  fun fromUpdateDtoToNewEntity(dto: UpdateInPrisonInterestsDto): InPrisonInterestsEntity = with(dto) {
    InPrisonInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewWorkInterests(dto.inPrisonWorkInterests, this)
      addNewTrainingInterests(dto.inPrisonTrainingInterests, this)
    }
  }

  private fun addNewWorkInterests(workInterests: List<InPrisonWorkInterest>, entity: InPrisonInterestsEntity) {
    entity.addWorkInterestChildren(workInterests.map { workInterestEntityMapper.fromDomainToEntity(it) })
  }

  private fun addNewTrainingInterests(trainingInterests: List<InPrisonTrainingInterest>, entity: InPrisonInterestsEntity) {
    entity.addTrainingInterestChildren(trainingInterests.map { trainingInterestEntityMapper.fromDomainToEntity(it) })
  }

  private fun updateExistingWorkInterests(
    existingEntities: MutableList<InPrisonWorkInterestEntity>,
    updatedDomain: List<InPrisonWorkInterest>,
  ) {
    val updatedDomainKeys = updatedDomain.map { it.workType.name }
    existingEntities
      .filter { entity -> updatedDomainKeys.contains(entity.workType.name) }
      .onEach { entity ->
        workInterestEntityMapper.updateEntityFromDomain(
          entity,
          updatedDomain.first { dto -> dto.workType.name == entity.workType.name },
        )
      }
  }

  private fun addNewWorkInterests(
    inPrisonInterestsEntity: InPrisonInterestsEntity,
    existingEntities: MutableList<InPrisonWorkInterestEntity>,
    updatedDomain: List<InPrisonWorkInterest>,
  ) {
    val currentIdentifiers = existingEntities.map { it.workType.name }

    val newEntities = updatedDomain
      .filter { dto -> !currentIdentifiers.contains(dto.workType.name) }
      .map { newDto -> workInterestEntityMapper.fromDomainToEntity(newDto) }

    inPrisonInterestsEntity.addWorkInterestChildren(newEntities)
  }

  private fun deleteRemovedWorkInterests(
    existingEntities: MutableList<InPrisonWorkInterestEntity>,
    updatedDomain: List<InPrisonWorkInterest>,
  ) {
    val updatedIdentifiers = updatedDomain.map { it.workType.name }

    val removedEntities = existingEntities.filter { entity -> !updatedIdentifiers.contains(entity.workType.name) }
    if (removedEntities.isNotEmpty()) {
      existingEntities.removeAll(removedEntities)
    }
  }

  private fun updateExistingTrainingInterests(
    existingEntities: MutableList<InPrisonTrainingInterestEntity>,
    updatedDomain: List<InPrisonTrainingInterest>,
  ) {
    val updatedDomainKeys = updatedDomain.map { it.trainingType.name }
    existingEntities
      .filter { entity -> updatedDomainKeys.contains(entity.trainingType.name) }
      .onEach { entity ->
        trainingInterestEntityMapper.updateEntityFromDomain(
          entity,
          updatedDomain.first { dto -> dto.trainingType.name == entity.trainingType.name },
        )
      }
  }

  private fun addNewTrainingInterests(
    inPrisonInterestsEntity: InPrisonInterestsEntity,
    existingEntities: MutableList<InPrisonTrainingInterestEntity>,
    updatedDomain: List<InPrisonTrainingInterest>,
  ) {
    val currentIdentifiers = existingEntities.map { it.trainingType.name }

    val newEntities = updatedDomain
      .filter { dto -> !currentIdentifiers.contains(dto.trainingType.name) }
      .map { newDto -> trainingInterestEntityMapper.fromDomainToEntity(newDto) }

    inPrisonInterestsEntity.addTrainingInterestChildren(newEntities)
  }

  private fun deleteRemovedTrainingInterests(
    existingEntities: MutableList<InPrisonTrainingInterestEntity>,
    updatedDomain: List<InPrisonTrainingInterest>,
  ) {
    val updatedIdentifiers = updatedDomain.map { it.trainingType.name }

    val removedEntities = existingEntities.filter { entity -> !updatedIdentifiers.contains(entity.trainingType.name) }
    if (removedEntities.isNotEmpty()) {
      existingEntities.removeAll(removedEntities)
    }
  }
}

@Component
class InPrisonWorkInterestEntityMapper {
  fun fromDomainToEntity(domain: InPrisonWorkInterest): InPrisonWorkInterestEntity = with(domain) {
    InPrisonWorkInterestEntity(
      reference = UUID.randomUUID(),
      workType = toWorkType(workType),
      workTypeOther = workTypeOther,
    )
  }

  fun fromEntityToDomain(persistedEntity: InPrisonWorkInterestEntity): InPrisonWorkInterest = with(persistedEntity) {
    InPrisonWorkInterest(
      workType = toWorkType(workType),
      workTypeOther = workTypeOther,
    )
  }

  fun updateEntityFromDomain(entity: InPrisonWorkInterestEntity, domain: InPrisonWorkInterest) = with(entity) {
    workType = toWorkType(domain.workType)
    workTypeOther = domain.workTypeOther
  }

  private fun toWorkType(workType: InPrisonWorkTypeEntity): InPrisonWorkTypeDomain = when (workType) {
    InPrisonWorkTypeEntity.CLEANING_AND_HYGIENE -> InPrisonWorkTypeDomain.CLEANING_AND_HYGIENE
    InPrisonWorkTypeEntity.COMPUTERS_OR_DESK_BASED -> InPrisonWorkTypeDomain.COMPUTERS_OR_DESK_BASED
    InPrisonWorkTypeEntity.GARDENING_AND_OUTDOORS -> InPrisonWorkTypeDomain.GARDENING_AND_OUTDOORS
    InPrisonWorkTypeEntity.KITCHENS_AND_COOKING -> InPrisonWorkTypeDomain.KITCHENS_AND_COOKING
    InPrisonWorkTypeEntity.MAINTENANCE -> InPrisonWorkTypeDomain.MAINTENANCE
    InPrisonWorkTypeEntity.PRISON_LAUNDRY -> InPrisonWorkTypeDomain.PRISON_LAUNDRY
    InPrisonWorkTypeEntity.PRISON_LIBRARY -> InPrisonWorkTypeDomain.PRISON_LIBRARY
    InPrisonWorkTypeEntity.TEXTILES_AND_SEWING -> InPrisonWorkTypeDomain.TEXTILES_AND_SEWING
    InPrisonWorkTypeEntity.WELDING_AND_METALWORK -> InPrisonWorkTypeDomain.WELDING_AND_METALWORK
    InPrisonWorkTypeEntity.WOODWORK_AND_JOINERY -> InPrisonWorkTypeDomain.WOODWORK_AND_JOINERY
    InPrisonWorkTypeEntity.OTHER -> InPrisonWorkTypeDomain.OTHER
  }

  private fun toWorkType(workType: InPrisonWorkTypeDomain): InPrisonWorkTypeEntity = when (workType) {
    InPrisonWorkTypeDomain.CLEANING_AND_HYGIENE -> InPrisonWorkTypeEntity.CLEANING_AND_HYGIENE
    InPrisonWorkTypeDomain.COMPUTERS_OR_DESK_BASED -> InPrisonWorkTypeEntity.COMPUTERS_OR_DESK_BASED
    InPrisonWorkTypeDomain.GARDENING_AND_OUTDOORS -> InPrisonWorkTypeEntity.GARDENING_AND_OUTDOORS
    InPrisonWorkTypeDomain.KITCHENS_AND_COOKING -> InPrisonWorkTypeEntity.KITCHENS_AND_COOKING
    InPrisonWorkTypeDomain.MAINTENANCE -> InPrisonWorkTypeEntity.MAINTENANCE
    InPrisonWorkTypeDomain.PRISON_LAUNDRY -> InPrisonWorkTypeEntity.PRISON_LAUNDRY
    InPrisonWorkTypeDomain.PRISON_LIBRARY -> InPrisonWorkTypeEntity.PRISON_LIBRARY
    InPrisonWorkTypeDomain.TEXTILES_AND_SEWING -> InPrisonWorkTypeEntity.TEXTILES_AND_SEWING
    InPrisonWorkTypeDomain.WELDING_AND_METALWORK -> InPrisonWorkTypeEntity.WELDING_AND_METALWORK
    InPrisonWorkTypeDomain.WOODWORK_AND_JOINERY -> InPrisonWorkTypeEntity.WOODWORK_AND_JOINERY
    InPrisonWorkTypeDomain.OTHER -> InPrisonWorkTypeEntity.OTHER
  }
}

@Component
class InPrisonTrainingInterestEntityMapper {

  fun fromDomainToEntity(domain: InPrisonTrainingInterest): InPrisonTrainingInterestEntity = with(domain) {
    InPrisonTrainingInterestEntity(
      reference = UUID.randomUUID(),
      trainingType = toTrainingType(trainingType),
      trainingTypeOther = trainingTypeOther,
    )
  }

  fun fromEntityToDomain(persistedEntity: InPrisonTrainingInterestEntity): InPrisonTrainingInterest = with(persistedEntity) {
    InPrisonTrainingInterest(
      trainingType = toTrainingType(trainingType),
      trainingTypeOther = trainingTypeOther,
    )
  }

  fun updateEntityFromDomain(entity: InPrisonTrainingInterestEntity, domain: InPrisonTrainingInterest) = with(entity) {
    trainingType = toTrainingType(domain.trainingType)
    trainingTypeOther = domain.trainingTypeOther
  }

  private fun toTrainingType(trainingType: InPrisonTrainingTypeEntity): InPrisonTrainingTypeDomain = when (trainingType) {
    InPrisonTrainingTypeEntity.BARBERING_AND_HAIRDRESSING -> InPrisonTrainingTypeDomain.BARBERING_AND_HAIRDRESSING
    InPrisonTrainingTypeEntity.CATERING -> InPrisonTrainingTypeDomain.CATERING
    InPrisonTrainingTypeEntity.COMMUNICATION_SKILLS -> InPrisonTrainingTypeDomain.COMMUNICATION_SKILLS
    InPrisonTrainingTypeEntity.ENGLISH_LANGUAGE_SKILLS -> InPrisonTrainingTypeDomain.ENGLISH_LANGUAGE_SKILLS
    InPrisonTrainingTypeEntity.FORKLIFT_DRIVING -> InPrisonTrainingTypeDomain.FORKLIFT_DRIVING
    InPrisonTrainingTypeEntity.INTERVIEW_SKILLS -> InPrisonTrainingTypeDomain.INTERVIEW_SKILLS
    InPrisonTrainingTypeEntity.MACHINERY_TICKETS -> InPrisonTrainingTypeDomain.MACHINERY_TICKETS
    InPrisonTrainingTypeEntity.NUMERACY_SKILLS -> InPrisonTrainingTypeDomain.NUMERACY_SKILLS
    InPrisonTrainingTypeEntity.RUNNING_A_BUSINESS -> InPrisonTrainingTypeDomain.RUNNING_A_BUSINESS
    InPrisonTrainingTypeEntity.SOCIAL_AND_LIFE_SKILLS -> InPrisonTrainingTypeDomain.SOCIAL_AND_LIFE_SKILLS
    InPrisonTrainingTypeEntity.WELDING_AND_METALWORK -> InPrisonTrainingTypeDomain.WELDING_AND_METALWORK
    InPrisonTrainingTypeEntity.WOODWORK_AND_JOINERY -> InPrisonTrainingTypeDomain.WOODWORK_AND_JOINERY
    InPrisonTrainingTypeEntity.OTHER -> InPrisonTrainingTypeDomain.OTHER
  }

  private fun toTrainingType(trainingType: InPrisonTrainingTypeDomain): InPrisonTrainingTypeEntity = when (trainingType) {
    InPrisonTrainingTypeDomain.BARBERING_AND_HAIRDRESSING -> InPrisonTrainingTypeEntity.BARBERING_AND_HAIRDRESSING
    InPrisonTrainingTypeDomain.CATERING -> InPrisonTrainingTypeEntity.CATERING
    InPrisonTrainingTypeDomain.COMMUNICATION_SKILLS -> InPrisonTrainingTypeEntity.COMMUNICATION_SKILLS
    InPrisonTrainingTypeDomain.ENGLISH_LANGUAGE_SKILLS -> InPrisonTrainingTypeEntity.ENGLISH_LANGUAGE_SKILLS
    InPrisonTrainingTypeDomain.FORKLIFT_DRIVING -> InPrisonTrainingTypeEntity.FORKLIFT_DRIVING
    InPrisonTrainingTypeDomain.INTERVIEW_SKILLS -> InPrisonTrainingTypeEntity.INTERVIEW_SKILLS
    InPrisonTrainingTypeDomain.MACHINERY_TICKETS -> InPrisonTrainingTypeEntity.MACHINERY_TICKETS
    InPrisonTrainingTypeDomain.NUMERACY_SKILLS -> InPrisonTrainingTypeEntity.NUMERACY_SKILLS
    InPrisonTrainingTypeDomain.RUNNING_A_BUSINESS -> InPrisonTrainingTypeEntity.RUNNING_A_BUSINESS
    InPrisonTrainingTypeDomain.SOCIAL_AND_LIFE_SKILLS -> InPrisonTrainingTypeEntity.SOCIAL_AND_LIFE_SKILLS
    InPrisonTrainingTypeDomain.WELDING_AND_METALWORK -> InPrisonTrainingTypeEntity.WELDING_AND_METALWORK
    InPrisonTrainingTypeDomain.WOODWORK_AND_JOINERY -> InPrisonTrainingTypeEntity.WOODWORK_AND_JOINERY
    InPrisonTrainingTypeDomain.OTHER -> InPrisonTrainingTypeEntity.OTHER
  }
}
