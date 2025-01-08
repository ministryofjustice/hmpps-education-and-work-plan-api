package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateActionPlanTest : IntegrationTestBase() {

  companion object {
    const val URI_TEMPLATE = "/action-plans/{prisonNumber}"
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
      .hasDeveloperMessageContaining("Error on field 'goals[0].steps': rejected value [[]], Steps cannot be empty when creating a Goal")
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
  fun `should fail to create action plan given action plan already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createRequest = aValidCreateActionPlanRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
  fun `should create a new action plan with multiple goals`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createStepRequest1 = aValidCreateStepRequest(title = "Step 1 of Goal 1")
    val createGoalRequest1 = aValidCreateGoalRequest(title = "Goal 1", steps = listOf(createStepRequest1), notes = null)

    val createStepRequest2 = aValidCreateStepRequest(title = "Step 1 of Goal 2")
    val createGoalRequest2 = aValidCreateGoalRequest(title = "Goal 2", steps = listOf(createStepRequest2), notes = "Goal2 notes")

    val createActionPlanRequest = aValidCreateActionPlanRequest(goals = listOf(createGoalRequest1, createGoalRequest2))
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
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
    val actionPlan = getActionPlan(prisonNumber)
    assertThat(actionPlan)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(2)
      // Because the goals are returned in date created order, the first goal in the response SHOULD be Goal 2, because it was the most recently created.
      // Sometimes however CI runs fast enough that the 2 goals created have the same timestamp and therefore the ordering is indeterminate,
      // hence we have to assert that either the first or second is Goal 2
      .anyOfGoalNumber(1, 2) {
        it.hasTitle("Goal 2")
          .hasNumberOfSteps(1)
          .step(1) {
            it.hasTitle("Step 1 of Goal 2")
              .hasStatus(StepStatus.NOT_STARTED)
          }
          .hasGoalNote("Goal2 notes")
      }
      // The 2nd goal in the response data SHOULD be the first goal to have been created.
      // Sometimes however CI runs fast enough that the 2 goals created have the same timestamp and therefore the ordering is indeterminate,
      // hence we have to assert that either the first or second is Goal 1
      .anyOfGoalNumber(1, 2) {
        it.hasTitle("Goal 1")
          .hasNumberOfSteps(1)
          .step(1) {
            it.hasTitle("Step 1 of Goal 1")
              .hasStatus(StepStatus.NOT_STARTED)
          }
          .hasNoNotes()
      }
      .allGoals {
        it.wasCreatedAtPrison(createGoalRequest1.prisonId)
          .wasCreatedBy(dpsUsername)
          .hasCreatedByDisplayName(displayName)
          .wasUpdatedBy(dpsUsername)
          .hasUpdatedByDisplayName(displayName)
          .wasUpdatedAtPrison(createGoalRequest1.prisonId)
      }
  }

  @Test
  fun `should create a new action plan and not create a review schedule given the prisoner does not have an Induction created before the Action Plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val createActionPlanRequest = aValidCreateActionPlanRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = getActionPlan(prisonNumber)
    assertThat(actionPlan).isNotNull

    assertThat(reviewScheduleHistoryRepository.findAll()).isEmpty()
  }

  @Test
  fun `should create a new action plan and create the initial review schedule given the prisoner already has an induction created before the action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateInductionRequest())

    val createActionPlanRequest = aValidCreateActionPlanRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = getActionPlan(prisonNumber)
    assertThat(actionPlan).isNotNull

    // assert that there is an Action Plan Reviews object, and that it contains no completed reviews, and the latestReviewSchedule has a SCHEDULED status
    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .hasNumberOfCompletedReviews(0)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
      }
    val reviewScheduleReference = actionPlanReviews.latestReviewSchedule.reference

    assertThat(reviewScheduleHistoryRepository.findAllByReference(reviewScheduleReference)).isNotNull
    assertThat(reviewScheduleHistoryRepository.findAll()).size().isEqualTo(1)
  }

  @Test
  fun `should create action plan and create initial review schedule given prisoner already has an induction created before the action plan, is sentenced without a release date and has the indeterminate flag`() {
    // Given
    val prisonNumber = "X9999XX" // Prisoner X9999XX is sentenced, but with no release date, and the has the `indeterminate` flag set
    createInduction(prisonNumber, aValidCreateInductionRequest())

    val createActionPlanRequest = aValidCreateActionPlanRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = getActionPlan(prisonNumber)
    assertThat(actionPlan).isNotNull

    // assert that there is an Action Plan Reviews object, and that it contains no completed reviews, and the latestReviewSchedule has a SCHEDULED status
    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .hasNumberOfCompletedReviews(0)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
      }
    val reviewScheduleReference = actionPlanReviews.latestReviewSchedule.reference

    assertThat(reviewScheduleHistoryRepository.findAllByReference(reviewScheduleReference)).isNotNull
    assertThat(reviewScheduleHistoryRepository.findAll()).size().isEqualTo(1)
  }

  @Test
  fun `should create action plan and not create initial review schedule given prisoner already has an induction created before the action plan, but is an unsupported sentence type for the release schedule`() {
    // Given
    val prisonNumber = "Z9999ZZ" // Prisoner Z9999ZZ is sentenced, but with no release date, which is an unsupported combination when creating the release schedule
    createInduction(prisonNumber, aValidCreateInductionRequest())

    val createActionPlanRequest = aValidCreateActionPlanRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = getActionPlan(prisonNumber)
    assertThat(actionPlan).isNotNull

    assertThat(reviewScheduleHistoryRepository.findAll()).isEmpty()
  }
}
