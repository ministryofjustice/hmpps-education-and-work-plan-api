package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidGoalResponse
import java.time.LocalDate
import java.util.stream.Stream
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ReviewDateCategory as ReviewDateCategoryDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewDateCategory as ReviewDateCategoryModel

@ExtendWith(MockitoExtension::class)
internal class ActionPlanResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: ActionPlanResourceMapperImpl

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @ParameterizedTest
  @MethodSource("reviewDateCriteria")
  fun `should map from model to domain`(
    sourceReviewDateCategory: ReviewDateCategoryModel,
    sourceReviewDate: LocalDate?,
    expectedReviewDateCategory: ReviewDateCategoryDomain,
    expectedReviewDate: LocalDate?,
  ) {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val request = aValidCreateActionPlanRequest(reviewDateCategory = sourceReviewDateCategory, reviewDate = sourceReviewDate)
    val expectedGoal = aValidGoal()
    val expectedActionPlan = aValidActionPlan(
      prisonNumber = prisonNumber,
      reviewDateCategory = expectedReviewDateCategory,
      reviewDate = expectedReviewDate,
      goals = listOf(expectedGoal),
    )
    given(goalMapper.fromModelToDomain(any<CreateGoalRequest>())).willReturn(expectedGoal)

    // When
    val actual = mapper.fromModelToDomain(prisonNumber, request)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedActionPlan)
    verify(goalMapper).fromModelToDomain(request.goals[0])
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val actionPlan = aValidActionPlan()
    val expectedGoal = aValidGoalResponse()
    val expectedActionPlan = aValidActionPlanResponse(
      reference = actionPlan.reference,
      prisonNumber = actionPlan.prisonNumber,
      reviewDateCategory = ReviewDateCategoryModel.SPECIFIC_DATE,
      reviewDate = actionPlan.reviewDate,
      goals = mutableListOf(expectedGoal),
    )
    given(goalMapper.fromDomainToModel(any())).willReturn(expectedGoal)

    // When
    val actual = mapper.fromDomainToModel(actionPlan)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedActionPlan)
    verify(goalMapper).fromDomainToModel(actionPlan.goals[0])
  }

  companion object {
    @JvmStatic
    private fun reviewDateCriteria(): Stream<Arguments> {
      val now = LocalDate.now()
      return Stream.of(
        Arguments.of(ReviewDateCategoryModel.THREE_MONTHS, null, ReviewDateCategoryDomain.THREE_MONTHS, now.plusMonths(3)),
        Arguments.of(ReviewDateCategoryModel.SIX_MONTHS, null, ReviewDateCategoryDomain.SIX_MONTHS, now.plusMonths(6)),
        Arguments.of(ReviewDateCategoryModel.TWELVE_MONTHS, null, ReviewDateCategoryDomain.TWELVE_MONTHS, now.plusMonths(12)),
        Arguments.of(ReviewDateCategoryModel.NO_DATE, null, ReviewDateCategoryDomain.NO_DATE, null),
        Arguments.of(ReviewDateCategoryModel.SPECIFIC_DATE, now.plusMonths(3), ReviewDateCategoryDomain.SPECIFIC_DATE, now.plusMonths(3)),
      )
    }
  }
}
