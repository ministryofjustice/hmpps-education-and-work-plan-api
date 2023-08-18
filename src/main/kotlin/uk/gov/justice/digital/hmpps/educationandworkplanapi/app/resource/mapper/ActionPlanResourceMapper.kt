package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory.NO_DATE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory.SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory.SPECIFIC_DATE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory.THREE_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory.TWELVE_MONTHS
import java.time.LocalDate

@Mapper(
  uses = [
    GoalResourceMapper::class,
  ],
)
abstract class ActionPlanResourceMapper {

  @Mapping(target = "reference", expression = "java(UUID.randomUUID())")
  @Mapping(target = "reviewDate", expression = "java(populateReviewDate(request))")
  abstract fun fromModelToDomain(prisonNumber: String, request: CreateActionPlanRequest): ActionPlan

  abstract fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse

  fun populateReviewDate(request: CreateActionPlanRequest): LocalDate? =
    when (request.reviewDateCategory) {
      THREE_MONTHS -> LocalDate.now().plusMonths(3)
      SIX_MONTHS -> LocalDate.now().plusMonths(6)
      TWELVE_MONTHS -> LocalDate.now().plusMonths(12)
      NO_DATE -> null
      SPECIFIC_DATE -> request.reviewDate
    }
}
