package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
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
      .bearerToken(aValidTokenWithAuthority(GOALS_RO, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
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
  fun `should add goal to prisoner's existing action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(
      notes = "a second note",
    )
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
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
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      // Event would be triggered twice - once for the original goal create, and once for the new goal created
      verify(telemetryClient, times(2)).trackEvent(
        eq("goal-created"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val createGoalEventProperties = eventPropertiesCaptor.secondValue
      assertThat(createGoalEventProperties)
        .containsEntry("status", "ACTIVE")
        .containsEntry("stepCount", "2")
        .containsEntry("reference", goal.goalReference.toString())
        .containsKey("correlationId")
    }

    val notes1 = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(actual.goals[0].goalReference, EntityType.GOAL, NoteType.GOAL)
    assertThat(notes1.size).isGreaterThan(0)
    assertThat(notes1[0].content).isEqualTo("a second note")

    val notes2 = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(actual.goals[1].goalReference, EntityType.GOAL, NoteType.GOAL)
    assertThat(notes2.size).isGreaterThan(0)
    assertThat(notes2[0].content).isEqualTo("Chris would like to improve his listening skills, not just his verbal communication")
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
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      // Creating the Action Plan created 1 goal, and we have just created another, which is why we now expect there to be 2 goals.
      .hasNumberOfGoals(2)
      // The goals are returned in date order descending, so even though we are interested in the 2nd goal to be created, it is element 1 in the returned data
      .goal(1) { goal ->
        goal.hasTitle(createGoalRequest.title)
          .hasNumberOfSteps(createGoalRequest.steps.size)
          .hasNoNotes()
      }

    val goal = actual.goals[0]
    await.untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      // Event would be triggered twice - once for the original goal create, and once for the new goal created
      verify(telemetryClient, times(2)).trackEvent(
        eq("goal-created"),
        capture(eventPropertiesCaptor),
        isNull(),
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

  @Test
  fun `should add goal with no notes`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(
      notes = null,
    )
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      // Creating the Action Plan created 1 goal, and we have just created another, which is why we now expect there to be 2 goals.
      .hasNumberOfGoals(2)
      // The goals are returned in date order descending, so even though we are interested in the 2nd goal to be created, it is element 1 in the returned data
      .goal(1) { goal ->
        goal
          .hasNoGoalNote()
          .hasNoNotes()
      }
  }

  @Test
  fun `should add goal with notes`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createGoalRequest = aValidCreateGoalRequest(
      notes = "This feels like an appropriate and achievable goal for Chris",
    )
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))

    // When
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      // Creating the Action Plan created 1 goal, and we have just created another, which is why we now expect there to be 2 goals.
      .hasNumberOfGoals(2)
      // The goals are returned in date order descending, so even though we are interested in the 2nd goal to be created, it is element 1 in the returned data
      .goal(1) { goal ->
        goal.hasGoalNote("This feels like an appropriate and achievable goal for Chris")
      }
  }
}
