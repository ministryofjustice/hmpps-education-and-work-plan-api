package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType as TrainingTypeApi

@Component
class PreviousTrainingResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toCreatePreviousTrainingDto(request: CreatePreviousTrainingRequest, prisonId: String): CreatePreviousTrainingDto =
    with(request) {
      CreatePreviousTrainingDto(
        prisonId = prisonId,
        trainingTypes = trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = trainingTypeOther,
      )
    }

  fun toPreviousTrainingResponse(previousTraining: PreviousTraining): PreviousTrainingResponse =
    with(previousTraining) {
      PreviousTrainingResponse(
        reference = reference,
        trainingTypes = trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = trainingTypeOther,
        createdBy = createdBy!!,
        createdByDisplayName = userService.getUserDetails(createdBy!!).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        createdAtPrison = createdAtPrison,
        updatedBy = lastUpdatedBy!!,
        updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
        updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
        updatedAtPrison = lastUpdatedAtPrison,
      )
    }

  fun toUpdatePreviousTrainingDto(request: UpdatePreviousTrainingRequest, prisonId: String): UpdatePreviousTrainingDto =
    with(request) {
      UpdatePreviousTrainingDto(
        reference = reference,
        prisonId = prisonId,
        trainingTypes = trainingTypes.map { toTrainingType(it) },
        trainingTypeOther = trainingTypeOther,
      )
    }

  private fun toTrainingType(trainingType: TrainingTypeApi): TrainingTypeDomain =
    when (trainingType) {
      TrainingTypeApi.CSCS_CARD -> TrainingTypeDomain.CSCS_CARD
      TrainingTypeApi.FIRST_AID_CERTIFICATE -> TrainingTypeDomain.FIRST_AID_CERTIFICATE
      TrainingTypeApi.FOOD_HYGIENE_CERTIFICATE -> TrainingTypeDomain.FOOD_HYGIENE_CERTIFICATE
      TrainingTypeApi.FULL_UK_DRIVING_LICENCE -> TrainingTypeDomain.FULL_UK_DRIVING_LICENCE
      TrainingTypeApi.HEALTH_AND_SAFETY -> TrainingTypeDomain.HEALTH_AND_SAFETY
      TrainingTypeApi.HGV_LICENCE -> TrainingTypeDomain.HGV_LICENCE
      TrainingTypeApi.MACHINERY_TICKETS -> TrainingTypeDomain.MACHINERY_TICKETS
      TrainingTypeApi.MANUAL_HANDLING -> TrainingTypeDomain.MANUAL_HANDLING
      TrainingTypeApi.TRADE_COURSE -> TrainingTypeDomain.TRADE_COURSE
      TrainingTypeApi.OTHER -> TrainingTypeDomain.OTHER
      TrainingTypeApi.NONE -> TrainingTypeDomain.NONE
    }

  private fun toTrainingType(trainingType: TrainingTypeDomain): TrainingTypeApi =
    when (trainingType) {
      TrainingTypeDomain.CSCS_CARD -> TrainingTypeApi.CSCS_CARD
      TrainingTypeDomain.FIRST_AID_CERTIFICATE -> TrainingTypeApi.FIRST_AID_CERTIFICATE
      TrainingTypeDomain.FOOD_HYGIENE_CERTIFICATE -> TrainingTypeApi.FOOD_HYGIENE_CERTIFICATE
      TrainingTypeDomain.FULL_UK_DRIVING_LICENCE -> TrainingTypeApi.FULL_UK_DRIVING_LICENCE
      TrainingTypeDomain.HEALTH_AND_SAFETY -> TrainingTypeApi.HEALTH_AND_SAFETY
      TrainingTypeDomain.HGV_LICENCE -> TrainingTypeApi.HGV_LICENCE
      TrainingTypeDomain.MACHINERY_TICKETS -> TrainingTypeApi.MACHINERY_TICKETS
      TrainingTypeDomain.MANUAL_HANDLING -> TrainingTypeApi.MANUAL_HANDLING
      TrainingTypeDomain.TRADE_COURSE -> TrainingTypeApi.TRADE_COURSE
      TrainingTypeDomain.OTHER -> TrainingTypeApi.OTHER
      TrainingTypeDomain.NONE -> TrainingTypeApi.NONE
    }
}
