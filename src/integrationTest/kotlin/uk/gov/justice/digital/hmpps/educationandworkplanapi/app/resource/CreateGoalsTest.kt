package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.anotherValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
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
    val stepRequest = aValidCreateStepRequest()
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
              .hasStatus(StepStatus.NOT_STARTED)
          }
      }

    val goal = actionPlanResponse.goals[0]
    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      // Event should be triggered only once for the new goal created
      verify(telemetryClient, times(1)).trackEvent(
        eq("goal-created"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createGoalEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createGoalEventProperties)
        .containsEntry("status", "ACTIVE")
        .containsEntry("stepCount", "1")
        .containsEntry("reference", goal.goalReference.toString())
        .containsEntry("notesCharacterCount", "23")
        .containsKey("correlationId")
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
    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      // Event would be triggered twice - once for the original goal create, and once for the new goal created
      verify(telemetryClient, times(2)).trackEvent(
        eq("goal-created"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createGoalEventProperties = eventPropertiesCaptor.secondValue
      assertThat(createGoalEventProperties)
        .containsEntry("status", "ACTIVE")
        .containsEntry("stepCount", "2")
        .containsEntry("reference", goal.goalReference.toString())
        .containsEntry("notesCharacterCount", "83")
        .containsKey("correlationId")
    }
  }

  @Test
  fun `should add goal with only mandatory fields populated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(
      notes = null,
      steps = listOf(
        aValidCreateStepRequest(),
        anotherValidCreateStepRequest(),
      ),
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
      .goal(1) { goal -> // goals are returned in creation date descending order, so to get the most recently added goal we need the first goal
        goal.hasTitle(createGoalRequest.title)
          .hasNumberOfSteps(createGoalRequest.steps.size)
          .hasNoNotes()
      }

    val goal = actual.goals[0]
    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      // Event would be triggered twice - once for the original goal create, and once for the new goal created
      verify(telemetryClient, times(2)).trackEvent(
        eq("goal-created"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createGoalEventProperties = eventPropertiesCaptor.secondValue
      assertThat(createGoalEventProperties)
        .containsEntry("status", "ACTIVE")
        .containsEntry("stepCount", "2")
        .containsEntry("reference", goal.goalReference.toString())
        .containsEntry("notesCharacterCount", "0")
        .containsKey("correlationId")
    }
  }
}
