package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingInterest as InPrisonTrainingInterestDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkInterest as InPrisonWorkInterestDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkType as InPrisonWorkTypeDomain

@Component
class InPrisonInterestsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toInPrisonInterestsResponse(inPrisonInterests: InPrisonInterests): InPrisonInterestsResponse? = with(inPrisonInterests) {
    InPrisonInterestsResponse(
      reference = reference,
      createdBy = createdBy,
      createdByDisplayName = userService.getUserDetails(createdBy).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
      inPrisonWorkInterests = inPrisonWorkInterests.map { toPrisonWorkInterest(it) },
      inPrisonTrainingInterests = inPrisonTrainingInterests.map { toPrisonTrainingInterest(it) },
    )
  }

  fun toCreateInPrisonInterestsDto(
    request: CreateInPrisonInterestsRequest,
    prisonId: String,
  ): CreateInPrisonInterestsDto = with(request) {
    CreateInPrisonInterestsDto(
      prisonId = prisonId,
      inPrisonWorkInterests = inPrisonWorkInterests.map { toPrisonWorkInterest(it) },
      inPrisonTrainingInterests = inPrisonTrainingInterests.map { toPrisonTrainingInterest(it) },
    )
  }

  fun toUpdateInPrisonInterestsDto(
    request: UpdateInPrisonInterestsRequest,
    prisonId: String,
  ): UpdateInPrisonInterestsDto = with(request) {
    UpdateInPrisonInterestsDto(
      prisonId = prisonId,
      reference = reference,
      inPrisonWorkInterests = inPrisonWorkInterests.map { toPrisonWorkInterest(it) },
      inPrisonTrainingInterests = inPrisonTrainingInterests.map { toPrisonTrainingInterest(it) },
    )
  }

  private fun toPrisonTrainingInterest(inPrisonTrainingInterest: InPrisonTrainingInterest): InPrisonTrainingInterestDomain = with(inPrisonTrainingInterest) {
    InPrisonTrainingInterestDomain(
      trainingTypeOther = trainingTypeOther,
      trainingType = toInPrisonTrainingType(trainingType),
    )
  }

  private fun toPrisonWorkInterest(inPrisonWorkInterest: InPrisonWorkInterest): InPrisonWorkInterestDomain = with(inPrisonWorkInterest) {
    InPrisonWorkInterestDomain(
      workTypeOther = workTypeOther,
      workType = toInPrisonWorkType(workType),
    )
  }

  private fun toInPrisonTrainingType(inPrisonTrainingType: InPrisonTrainingType): InPrisonTrainingTypeDomain = when (inPrisonTrainingType) {
    InPrisonTrainingType.BARBERING_AND_HAIRDRESSING -> InPrisonTrainingTypeDomain.BARBERING_AND_HAIRDRESSING
    InPrisonTrainingType.CATERING -> InPrisonTrainingTypeDomain.CATERING
    InPrisonTrainingType.COMMUNICATION_SKILLS -> InPrisonTrainingTypeDomain.COMMUNICATION_SKILLS
    InPrisonTrainingType.ENGLISH_LANGUAGE_SKILLS -> InPrisonTrainingTypeDomain.ENGLISH_LANGUAGE_SKILLS
    InPrisonTrainingType.FORKLIFT_DRIVING -> InPrisonTrainingTypeDomain.FORKLIFT_DRIVING
    InPrisonTrainingType.INTERVIEW_SKILLS -> InPrisonTrainingTypeDomain.INTERVIEW_SKILLS
    InPrisonTrainingType.MACHINERY_TICKETS -> InPrisonTrainingTypeDomain.MACHINERY_TICKETS
    InPrisonTrainingType.NUMERACY_SKILLS -> InPrisonTrainingTypeDomain.NUMERACY_SKILLS
    InPrisonTrainingType.RUNNING_A_BUSINESS -> InPrisonTrainingTypeDomain.RUNNING_A_BUSINESS
    InPrisonTrainingType.SOCIAL_AND_LIFE_SKILLS -> InPrisonTrainingTypeDomain.SOCIAL_AND_LIFE_SKILLS
    InPrisonTrainingType.WELDING_AND_METALWORK -> InPrisonTrainingTypeDomain.WELDING_AND_METALWORK
    InPrisonTrainingType.WOODWORK_AND_JOINERY -> InPrisonTrainingTypeDomain.WOODWORK_AND_JOINERY
    InPrisonTrainingType.OTHER -> InPrisonTrainingTypeDomain.OTHER
  }

  private fun toInPrisonWorkType(inPrisonWorkType: InPrisonWorkType): InPrisonWorkTypeDomain = when (inPrisonWorkType) {
    InPrisonWorkType.CLEANING_AND_HYGIENE -> InPrisonWorkTypeDomain.CLEANING_AND_HYGIENE
    InPrisonWorkType.COMPUTERS_OR_DESK_BASED -> InPrisonWorkTypeDomain.COMPUTERS_OR_DESK_BASED
    InPrisonWorkType.GARDENING_AND_OUTDOORS -> InPrisonWorkTypeDomain.GARDENING_AND_OUTDOORS
    InPrisonWorkType.KITCHENS_AND_COOKING -> InPrisonWorkTypeDomain.KITCHENS_AND_COOKING
    InPrisonWorkType.MAINTENANCE -> InPrisonWorkTypeDomain.MAINTENANCE
    InPrisonWorkType.PRISON_LAUNDRY -> InPrisonWorkTypeDomain.PRISON_LAUNDRY
    InPrisonWorkType.PRISON_LIBRARY -> InPrisonWorkTypeDomain.PRISON_LIBRARY
    InPrisonWorkType.TEXTILES_AND_SEWING -> InPrisonWorkTypeDomain.TEXTILES_AND_SEWING
    InPrisonWorkType.WELDING_AND_METALWORK -> InPrisonWorkTypeDomain.WELDING_AND_METALWORK
    InPrisonWorkType.WOODWORK_AND_JOINERY -> InPrisonWorkTypeDomain.WOODWORK_AND_JOINERY
    InPrisonWorkType.OTHER -> InPrisonWorkTypeDomain.OTHER
  }

  private fun toPrisonTrainingInterest(inPrisonTrainingInterestDomain: InPrisonTrainingInterestDomain): InPrisonTrainingInterest = with(inPrisonTrainingInterestDomain) {
    InPrisonTrainingInterest(
      trainingTypeOther = trainingTypeOther,
      trainingType = toInPrisonTrainingTypeDomain(trainingType),
    )
  }

  private fun toPrisonWorkInterest(inPrisonWorkInterestDomain: InPrisonWorkInterestDomain): InPrisonWorkInterest = with(inPrisonWorkInterestDomain) {
    InPrisonWorkInterest(
      workTypeOther = workTypeOther,
      workType = toInPrisonWorkTypeDomain(workType),
    )
  }

  private fun toInPrisonTrainingTypeDomain(inPrisonTrainingTypeDomain: InPrisonTrainingTypeDomain): InPrisonTrainingType = when (inPrisonTrainingTypeDomain) {
    InPrisonTrainingTypeDomain.BARBERING_AND_HAIRDRESSING -> InPrisonTrainingType.BARBERING_AND_HAIRDRESSING
    InPrisonTrainingTypeDomain.CATERING -> InPrisonTrainingType.CATERING
    InPrisonTrainingTypeDomain.COMMUNICATION_SKILLS -> InPrisonTrainingType.COMMUNICATION_SKILLS
    InPrisonTrainingTypeDomain.ENGLISH_LANGUAGE_SKILLS -> InPrisonTrainingType.ENGLISH_LANGUAGE_SKILLS
    InPrisonTrainingTypeDomain.FORKLIFT_DRIVING -> InPrisonTrainingType.FORKLIFT_DRIVING
    InPrisonTrainingTypeDomain.INTERVIEW_SKILLS -> InPrisonTrainingType.INTERVIEW_SKILLS
    InPrisonTrainingTypeDomain.MACHINERY_TICKETS -> InPrisonTrainingType.MACHINERY_TICKETS
    InPrisonTrainingTypeDomain.NUMERACY_SKILLS -> InPrisonTrainingType.NUMERACY_SKILLS
    InPrisonTrainingTypeDomain.RUNNING_A_BUSINESS -> InPrisonTrainingType.RUNNING_A_BUSINESS
    InPrisonTrainingTypeDomain.SOCIAL_AND_LIFE_SKILLS -> InPrisonTrainingType.SOCIAL_AND_LIFE_SKILLS
    InPrisonTrainingTypeDomain.WELDING_AND_METALWORK -> InPrisonTrainingType.WELDING_AND_METALWORK
    InPrisonTrainingTypeDomain.WOODWORK_AND_JOINERY -> InPrisonTrainingType.WOODWORK_AND_JOINERY
    InPrisonTrainingTypeDomain.OTHER -> InPrisonTrainingType.OTHER
  }

  private fun toInPrisonWorkTypeDomain(inPrisonWorkTypeDomain: InPrisonWorkTypeDomain): InPrisonWorkType = when (inPrisonWorkTypeDomain) {
    InPrisonWorkTypeDomain.CLEANING_AND_HYGIENE -> InPrisonWorkType.CLEANING_AND_HYGIENE
    InPrisonWorkTypeDomain.COMPUTERS_OR_DESK_BASED -> InPrisonWorkType.COMPUTERS_OR_DESK_BASED
    InPrisonWorkTypeDomain.GARDENING_AND_OUTDOORS -> InPrisonWorkType.GARDENING_AND_OUTDOORS
    InPrisonWorkTypeDomain.KITCHENS_AND_COOKING -> InPrisonWorkType.KITCHENS_AND_COOKING
    InPrisonWorkTypeDomain.MAINTENANCE -> InPrisonWorkType.MAINTENANCE
    InPrisonWorkTypeDomain.PRISON_LAUNDRY -> InPrisonWorkType.PRISON_LAUNDRY
    InPrisonWorkTypeDomain.PRISON_LIBRARY -> InPrisonWorkType.PRISON_LIBRARY
    InPrisonWorkTypeDomain.TEXTILES_AND_SEWING -> InPrisonWorkType.TEXTILES_AND_SEWING
    InPrisonWorkTypeDomain.WELDING_AND_METALWORK -> InPrisonWorkType.WELDING_AND_METALWORK
    InPrisonWorkTypeDomain.WOODWORK_AND_JOINERY -> InPrisonWorkType.WOODWORK_AND_JOINERY
    InPrisonWorkTypeDomain.OTHER -> InPrisonWorkType.OTHER
  }
}
