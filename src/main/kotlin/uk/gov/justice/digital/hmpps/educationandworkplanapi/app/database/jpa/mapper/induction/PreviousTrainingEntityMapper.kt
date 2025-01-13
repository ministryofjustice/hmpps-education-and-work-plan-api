package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity

@Component
class PreviousTrainingEntityMapper {

  fun fromCreateDtoToEntity(dto: CreatePreviousTrainingDto): PreviousTrainingEntity =
    with(dto) {
      PreviousTrainingEntity(
        reference = UUID.randomUUID(),
        trainingTypes = trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = trainingTypeOther,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      )
    }

  fun fromEntityToDomain(persistedEntity: PreviousTrainingEntity): PreviousTraining =
    with(persistedEntity) {
      PreviousTraining(
        reference = reference,
        trainingTypes = trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = trainingTypeOther,
        createdAt = createdAt!!,
        createdBy = createdBy!!,
        createdAtPrison = createdAtPrison,
        lastUpdatedAt = updatedAt!!,
        lastUpdatedBy = updatedBy!!,
        lastUpdatedAtPrison = updatedAtPrison,
      )
    }

  fun updateExistingEntityFromDto(entity: PreviousTrainingEntity, dto: UpdatePreviousTrainingDto?) =
    dto?.also {
      with(entity) {
        trainingTypes = it.trainingTypes.map { toTrainingType(it) }
        trainingTypeOther = it.trainingTypeOther
        updatedAtPrison = it.prisonId
      }
    }

  fun fromUpdateDtoToNewEntity(previousTraining: UpdatePreviousTrainingDto?): PreviousTrainingEntity? =
    previousTraining?.let {
      PreviousTrainingEntity(
        reference = UUID.randomUUID(),
        trainingTypes = it.trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = it.trainingTypeOther,
        createdAtPrison = it.prisonId,
        updatedAtPrison = it.prisonId,
      )
    }

  private fun toTrainingType(entity: TrainingTypeEntity): TrainingTypeDomain =
    when (entity) {
      TrainingTypeEntity.CSCS_CARD -> TrainingTypeDomain.CSCS_CARD
      TrainingTypeEntity.FIRST_AID_CERTIFICATE -> TrainingTypeDomain.FIRST_AID_CERTIFICATE
      TrainingTypeEntity.FOOD_HYGIENE_CERTIFICATE -> TrainingTypeDomain.FOOD_HYGIENE_CERTIFICATE
      TrainingTypeEntity.FULL_UK_DRIVING_LICENCE -> TrainingTypeDomain.FULL_UK_DRIVING_LICENCE
      TrainingTypeEntity.HEALTH_AND_SAFETY -> TrainingTypeDomain.HEALTH_AND_SAFETY
      TrainingTypeEntity.HGV_LICENCE -> TrainingTypeDomain.HGV_LICENCE
      TrainingTypeEntity.MACHINERY_TICKETS -> TrainingTypeDomain.MACHINERY_TICKETS
      TrainingTypeEntity.MANUAL_HANDLING -> TrainingTypeDomain.MANUAL_HANDLING
      TrainingTypeEntity.TRADE_COURSE -> TrainingTypeDomain.TRADE_COURSE
      TrainingTypeEntity.OTHER -> TrainingTypeDomain.OTHER
      TrainingTypeEntity.NONE -> TrainingTypeDomain.NONE
    }

  private fun toTrainingType(entity: TrainingTypeDomain): TrainingTypeEntity =
    when (entity) {
      TrainingTypeDomain.CSCS_CARD -> TrainingTypeEntity.CSCS_CARD
      TrainingTypeDomain.FIRST_AID_CERTIFICATE -> TrainingTypeEntity.FIRST_AID_CERTIFICATE
      TrainingTypeDomain.FOOD_HYGIENE_CERTIFICATE -> TrainingTypeEntity.FOOD_HYGIENE_CERTIFICATE
      TrainingTypeDomain.FULL_UK_DRIVING_LICENCE -> TrainingTypeEntity.FULL_UK_DRIVING_LICENCE
      TrainingTypeDomain.HEALTH_AND_SAFETY -> TrainingTypeEntity.HEALTH_AND_SAFETY
      TrainingTypeDomain.HGV_LICENCE -> TrainingTypeEntity.HGV_LICENCE
      TrainingTypeDomain.MACHINERY_TICKETS -> TrainingTypeEntity.MACHINERY_TICKETS
      TrainingTypeDomain.MANUAL_HANDLING -> TrainingTypeEntity.MANUAL_HANDLING
      TrainingTypeDomain.TRADE_COURSE -> TrainingTypeEntity.TRADE_COURSE
      TrainingTypeDomain.OTHER -> TrainingTypeEntity.OTHER
      TrainingTypeDomain.NONE -> TrainingTypeEntity.NONE
    }
}
