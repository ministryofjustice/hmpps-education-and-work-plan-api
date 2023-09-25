package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateGoalsTest : IntegrationTestBase() {

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateGoalsRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create goals given no goals provided`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateGoalsRequest(goals = emptyList())

    // When
    val response = webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
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
      .hasUserMessage("Validation failed for object='createGoalsRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Goals cannot be empty when creating Goals")
  }

  @Test
  fun `should fail to create goals given a goal with no steps provided`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateGoalsRequest(goals = listOf(aValidCreateGoalRequest(steps = emptyList())))

    // When
    val response = webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
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
      .hasUserMessage("Validation failed for object='createGoalsRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Steps cannot be empty when creating a Goal")
  }

  @Test
  fun `should fail to create goals given null fields`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
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
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property goals due to missing (therefore NULL) value for creator parameter goals")
  }

  @Test
  fun `should add goals and create a new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val stepRequest = aValidCreateStepRequest(targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS)
    val createGoalRequest = aValidCreateGoalRequest(steps = listOf(stepRequest), notes = "Notes about the goal...")
    val createGoalsRequest = aValidCreateGoalsRequest(
      goals = listOf(createGoalRequest),
    )

    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
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
    val actionPlanResponse = getActionPlan(prisonNumber)
    assertThat(actionPlanResponse)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal.hasTitle(createGoalRequest.title)
          .hasNumberOfSteps(createGoalRequest.steps.size)
          .wasCreatedAtPrison(createGoalRequest.prisonId)
          .wasCreatedBy(dpsUsername)
          .hasCreatedByDisplayName(displayName)
          .wasUpdatedAtPrison(createGoalRequest.prisonId)
          .wasUpdatedBy(dpsUsername)
          .hasUpdatedByDisplayName(displayName)
          .step(1) { step ->
            step.hasTitle(stepRequest.title)
              .hasTargetDateRange(TargetDateRange.ZERO_TO_THREE_MONTHS)
              .hasStatus(StepStatus.NOT_STARTED)
          }
      }

    val goal = actionPlanResponse.goals[0]
    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "1",
      "reference" to goal.goalReference.toString(),
      "notesCharacterCount" to "23",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }

    // assert timeline event is created successfully
    val prisonerTimeline = getTimeline(prisonNumber)
    assertThat(prisonerTimeline)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfEvents(1)
      .event(1) { event ->
        event.hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
          .hasSourceReference(actionPlanResponse.reference.toString())
          .hasNoContextualInfo()
      }
  }

  @Test
  fun `should add goal to prisoner's existing action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(2)

    val goal = actual.goals[0]
    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "2",
      "reference" to goal.goalReference.toString(),
      "notesCharacterCount" to "83",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }

    // assert timeline event is created successfully
    val prisonerTimeline = getTimeline(prisonNumber)
    assertThat(prisonerTimeline)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfEvents(2)
      .event(2) { event ->
        event.hasEventType(TimelineEventType.GOAL_CREATED)
          .hasSourceReference(goal.goalReference.toString())
          .hasContextualInfo(goal.title)
          .wasActionedBy("auser_gen")
          .hasActionedByDisplayName("Albert User")
          .hasPrisonId("BXI")
      }
  }

  @Test
  fun `should add goal with only mandatory fields populated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(notes = null)
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(2)
      .goal(1) { goal -> // goals are returned in creation date descending order, so to get the most recently added goal we need the first goal
        goal.hasTitle(createGoalRequest.title)
          .hasNumberOfSteps(createGoalRequest.steps.size)
          .hasNoNotes()
      }

    val goal = actual.goals[0]
    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "2",
      "reference" to goal.goalReference.toString(),
      "notesCharacterCount" to "0",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }
  }
}
