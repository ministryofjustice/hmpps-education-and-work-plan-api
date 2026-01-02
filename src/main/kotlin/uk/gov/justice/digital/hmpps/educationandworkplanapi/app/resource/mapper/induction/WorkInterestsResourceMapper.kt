package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

@Component
class WorkInterestsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toCreateFutureWorkInterestsDto(
    request: CreateFutureWorkInterestsRequest,
    prisonId: String,
  ): CreateFutureWorkInterestsDto = with(request) {
    CreateFutureWorkInterestsDto(
      prisonId = prisonId,
      interests = interests.map { toWorkInterests(it) },
    )
  }

  fun toFutureWorkInterestsResponse(futureWorkInterests: FutureWorkInterests): FutureWorkInterestsResponse? = with(futureWorkInterests) {
    FutureWorkInterestsResponse(
      reference = reference,
      createdBy = createdBy,
      createdByDisplayName = userService.getUserDetails(createdBy).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
      interests = interests.map { toFutureWorkInterests(it) },
    )
  }

  fun toUpdateFutureWorkInterestsDto(
    request: UpdateFutureWorkInterestsRequest,
    prisonId: String,
  ): UpdateFutureWorkInterestsDto = with(request) {
    UpdateFutureWorkInterestsDto(
      prisonId = prisonId,
      reference = reference,
      interests = interests.map { toWorkInterests(it) },
    )
  }

  private fun toFutureWorkInterests(workInterest: WorkInterest): FutureWorkInterest = with(workInterest) {
    FutureWorkInterest(workTypeOther = workTypeOther, role = role, workType = toWorkType(workType))
  }

  private fun toWorkInterests(futureWorkInterest: FutureWorkInterest): WorkInterest = with(futureWorkInterest) {
    WorkInterest(workTypeOther = workTypeOther, role = role, workType = toWorkInterestType(workType))
  }

  private fun toWorkInterestType(workType: WorkType): WorkInterestType = when (workType) {
    WorkType.OUTDOOR -> WorkInterestType.OUTDOOR
    WorkType.CONSTRUCTION -> WorkInterestType.CONSTRUCTION
    WorkType.DRIVING -> WorkInterestType.DRIVING
    WorkType.BEAUTY -> WorkInterestType.BEAUTY
    WorkType.HOSPITALITY -> WorkInterestType.HOSPITALITY
    WorkType.TECHNICAL -> WorkInterestType.TECHNICAL
    WorkType.MANUFACTURING -> WorkInterestType.MANUFACTURING
    WorkType.OFFICE -> WorkInterestType.OFFICE
    WorkType.RETAIL -> WorkInterestType.RETAIL
    WorkType.SPORTS -> WorkInterestType.SPORTS
    WorkType.WAREHOUSING -> WorkInterestType.WAREHOUSING
    WorkType.WASTE_MANAGEMENT -> WorkInterestType.WASTE_MANAGEMENT
    WorkType.EDUCATION_TRAINING -> WorkInterestType.EDUCATION_TRAINING
    WorkType.CLEANING_AND_MAINTENANCE -> WorkInterestType.CLEANING_AND_MAINTENANCE
    WorkType.OTHER -> WorkInterestType.OTHER
  }

  private fun toWorkType(workInterestType: WorkInterestType): WorkType = when (workInterestType) {
    WorkInterestType.OUTDOOR -> WorkType.OUTDOOR
    WorkInterestType.CONSTRUCTION -> WorkType.CONSTRUCTION
    WorkInterestType.DRIVING -> WorkType.DRIVING
    WorkInterestType.BEAUTY -> WorkType.BEAUTY
    WorkInterestType.HOSPITALITY -> WorkType.HOSPITALITY
    WorkInterestType.TECHNICAL -> WorkType.TECHNICAL
    WorkInterestType.MANUFACTURING -> WorkType.MANUFACTURING
    WorkInterestType.OFFICE -> WorkType.OFFICE
    WorkInterestType.RETAIL -> WorkType.RETAIL
    WorkInterestType.SPORTS -> WorkType.SPORTS
    WorkInterestType.WAREHOUSING -> WorkType.WAREHOUSING
    WorkInterestType.WASTE_MANAGEMENT -> WorkType.WASTE_MANAGEMENT
    WorkInterestType.EDUCATION_TRAINING -> WorkType.EDUCATION_TRAINING
    WorkInterestType.CLEANING_AND_MAINTENANCE -> WorkType.CLEANING_AND_MAINTENANCE
    WorkInterestType.OTHER -> WorkType.OTHER
  }
}
