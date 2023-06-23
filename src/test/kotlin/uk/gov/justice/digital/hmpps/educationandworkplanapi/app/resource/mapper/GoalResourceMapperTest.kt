package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalCategory
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidStepResponse
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalCategory as GoalCategoryApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi

@ExtendWith(MockitoExtension::class)
internal class GoalResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalResourceMapperImpl

  @Mock
  private lateinit var stepMapper: StepResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map from model to domain`() {
    // Given
    val createStepRequest = aValidCreateStepRequest()
    val createGoalRequest = aValidCreateGoalRequest(
      category = GoalCategoryApi.PERSONAL_DEVELOPMENT,
      steps = mutableListOf(createStepRequest),
    )
    val expectedStep = aValidStep()
    given(stepMapper.fromModelToDomain(any())).willReturn(expectedStep)
    val expectedGoal = aValidGoal(
      reference = UUID.randomUUID(),
      title = createGoalRequest.title,
      reviewDate = createGoalRequest.reviewDate,
      category = GoalCategory.PERSONAL_DEVELOPMENT,
      status = GoalStatus.ACTIVE,
      notes = createGoalRequest.notes,
      steps = mutableListOf(expectedStep),
      // JPA managed fields - expect these all to be null
      createdAt = null,
      createdBy = null,
      lastUpdatedAt = null,
      lastUpdatedBy = null,
    )

    // When
    val actual = mapper.fromModelToDomain(createGoalRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedGoal)
    assertThat(actual.reference).isNotNull()
    verify(stepMapper).fromModelToDomain(createStepRequest)
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val step = aValidStep()
    val goal = aValidGoal(
      category = GoalCategory.PERSONAL_DEVELOPMENT,
      status = GoalStatus.ACTIVE,
      steps = mutableListOf(step),
      createdBy = "a.user.id",
      lastUpdatedBy = "another.user.id",
    )
    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)
    val expected = aValidGoalResponse(
      reference = goal.reference,
      title = goal.title,
      reviewDate = goal.reviewDate,
      category = GoalCategoryApi.PERSONAL_DEVELOPMENT,
      status = GoalStatusApi.ACTIVE,
      notes = goal.notes,
      steps = listOf(expectedStepResponse),
      createdAt = expectedDateTime,
      createdBy = "a.user.id",
      updatedAt = expectedDateTime,
      updatedBy = "another.user.id",
    )

    // When
    val actual = mapper.fromDomainToModel(goal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
  }
}
