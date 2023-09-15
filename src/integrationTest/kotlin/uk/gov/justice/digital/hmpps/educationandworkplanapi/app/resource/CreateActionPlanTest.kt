package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TargetDateRange as TargetDateRangeEntity

class CreateActionPlanTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateActionPlanRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create action plan given no goals provided`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateActionPlanRequest(goals = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Goals cannot be empty when creating an Action Plan")
  }

  @Test
  fun `should fail to create action plan given a goal with no steps provided`() {
    val prisonNumber = aValidPrisonNumber()
    val goalWithNoSteps = aValidCreateGoalRequest(steps = emptyList())
    val createRequest = aValidCreateActionPlanRequest(goals = listOf(goalWithNoSteps))

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Error on field 'goals[0].steps': rejected value [[]], size must be between 1 and 2147483647")
  }

  @Test
  fun `should fail to create action plan given review date is in the past`() {
    val prisonNumber = aValidPrisonNumber()
    val invalidReviewDate = LocalDate.now().minusDays(1)
    val createRequest = aValidCreateActionPlanRequest(reviewDate = invalidReviewDate)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Error on field 'reviewDate': rejected value [$invalidReviewDate], Cannot be in the past")
  }

  @Test
  fun `should fail to create action plan given null fields`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property goals due to missing (therefore NULL) value for creator parameter goals")
  }

  @Test
  @Transactional
  fun `should fail to create action plan given action plan already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlanEntity(prisonNumber = prisonNumber)
    actionPlanRepository.save(actionPlan)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(actionPlan).hasNumberOfGoals(1)
    val createRequest = aValidCreateActionPlanRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("An Action Plan already exists for prisoner $prisonNumber.")
  }

  @Test
  @Transactional
  fun `should create a new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createStepRequest = aValidCreateStepRequest()
    val createGoalRequest = aValidCreateGoalRequest(steps = listOf(createStepRequest))
    val createActionPlanRequest = aValidCreateActionPlanRequest(goals = listOf(createGoalRequest))
    val expectedReviewDate = createActionPlanRequest.reviewDate!!
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan)
      .isForPrisonNumber(prisonNumber)
      .hasReviewDate(expectedReviewDate)
      .hasNumberOfGoals(1)
      .wasCreatedBy(dpsUsername)
    val goal = actionPlan!!.goals!![0]
    assertThat(goal)
      .hasTitle(createGoalRequest.title)
      .hasNumberOfSteps(createGoalRequest.steps.size)
      .wasCreatedAtPrison(createGoalRequest.prisonId)
      .wasCreatedBy(dpsUsername)
      .hasCreatedByDisplayName(displayName)
      .wasUpdatedBy(dpsUsername)
      .hasUpdatedByDisplayName(displayName)
      .wasUpdatedAtPrison(createGoalRequest.prisonId)
    val step = goal.steps!![0]
    assertThat(step)
      .hasTitle(createStepRequest.title)
      .hasTargetDateRange(TargetDateRangeEntity.ZERO_TO_THREE_MONTHS)
      .hasStatus(StepStatus.NOT_STARTED)
      .wasCreatedBy(dpsUsername)

    // assert timeline event is created successfully
    val prisonerTimeline = timelineRepository.findByPrisonNumber(prisonNumber)!!
    assertThat(prisonerTimeline.prisonNumber).isEqualTo(prisonNumber)
    val events = prisonerTimeline.events!!
    assertThat(events.size).isEqualTo(1)
    assertThat(events[0]).hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
    assertThat(events[0]).hasSourceReference(actionPlan.reference.toString())
    assertThat(events[0]).hasNoContextualInfo()
    assertThat(events[0]).hasAReference()
    assertThat(events[0]).hasJpaManagedFieldsPopulated()
  }

  @Test
  @Transactional
  fun `should create a new action plan with no review date`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest(reviewDate = null)
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan)
      .isForPrisonNumber(prisonNumber)
      .hasNoReviewDate()
      .wasCreatedBy(dpsUsername)
  }
}
