package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.isNull
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class UpdateGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, randomValidPrisonNumber(), aValidReference())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, randomValidPrisonNumber(), aValidReference())
      .withBody(aValidUpdateGoalRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to update goal given no steps provided`() {
    val prisonNumber = randomValidPrisonNumber()
    val goalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
      steps = emptyList(),
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='updateGoalRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Steps cannot be empty when updating a Goal")
  }

  @Test
  fun `should fail to update goal given null fields`() {
    val prisonNumber = randomValidPrisonNumber()
    val goalReference = aValidReference()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
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
      .hasUserMessageContaining("value failed for JSON property goalReference due to missing (therefore NULL) value for creator parameter goalReference")
  }

  @Test
  fun `should fail to update goal given goal does not exist`() {
    val prisonNumber = randomValidPrisonNumber()
    val goalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should fail to update goal given goal references in URI path and request body do not match`() {
    val prisonNumber = randomValidPrisonNumber()
    val goalReference = aValidReference()
    val someOtherGoalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = someOtherGoalReference,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessage("Goal reference in URI path must match the Goal reference in the request body")
  }

  @Test
  fun `should update goal`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequest())
    createActionPlan(
      username = "auser_gen",
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(
        goals = listOf(
          aValidCreateGoalRequest(
            title = "Learn French",
            steps = listOf(
              aValidCreateStepRequest(
                title = "Book course",
              ),
              aValidCreateStepRequest(
                title = "Attend course",
              ),
            ),
          ),
        ),
      ),
    )

    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference
    val step1 = goal.steps[0]
    val stepReference = step1.stepReference

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
      title = "Learn French to GCSE standard",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepReference,
          title = "Book course before December 2023",
          sequenceNumber = 1,
        ),
        aValidUpdateStepRequest(
          stepReference = null,
          title = "Attend course before March 2024",
          sequenceNumber = 2,
        ),
      ),
      notes = "Chris would like to improve his listening skills, not just his verbal communication - updated",
      prisonId = "MDI",
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          username = "buser_gen",
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasTitle("Learn French to GCSE standard")
          .hasNumberOfSteps(2)
          .step(1) { step ->
            step.hasTitle("Book course before December 2023")
          }
          .step(2) { step ->
            step.hasTitle("Attend course before March 2024")
          }
          .wasCreatedAtPrison("BXI")
          .wasUpdatedAtPrison("MDI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedBy("buser_gen")
          .hasGoalNote("Chris would like to improve his listening skills, not just his verbal communication - updated")
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .anyOfEventNumber(4, 5, 6) {
          // Due to exact timestamps during testing it could be 4/5/6th entry
          it.hasEventType(TimelineEventType.GOAL_UPDATED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = createCaptor<Map<String, String>>()

      verify(telemetryClient).trackEvent(
        eq("goal-updated"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      verify(telemetryClient).trackEvent(
        eq("step-removed"),
        capture(eventPropertiesCaptor),
        isNull(),
      )

      val goalUpdatedEventProperties = eventPropertiesCaptor.firstValue
      val stepRemovedEventProperties = eventPropertiesCaptor.secondValue
      assertThat(goalUpdatedEventProperties)
        .containsEntry("reference", goalReference.toString())
      assertThat(stepRemovedEventProperties)
        .containsEntry("reference", goalReference.toString())
        .containsEntry("stepCount", "2")
      assertThat(goalUpdatedEventProperties["correlationId"])
        .isEqualTo(stepRemovedEventProperties["correlationId"])
    }
  }

  @RepeatedTest(100)
  fun `should update goal and delete goal note`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequest())
    createActionPlan(
      username = "auser_gen",
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(
        goals = listOf(
          aValidCreateGoalRequest(
            title = "Learn French",
            steps = listOf(
              aValidCreateStepRequest(
                title = "Book course",
              ),
              aValidCreateStepRequest(
                title = "Attend course",
              ),
            ),
          ),
        ),
      ),
    )

    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference
    val step1 = goal.steps[0]
    val stepReference = step1.stepReference

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
      title = "Learn French to GCSE standard",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepReference,
          title = "Book course before December 2023",
          sequenceNumber = 1,
        ),
        aValidUpdateStepRequest(
          stepReference = null,
          title = "Attend course before March 2024",
          sequenceNumber = 2,
        ),
      ),
      notes = "",
      prisonId = "MDI",
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          username = "buser_gen",
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasTitle("Learn French to GCSE standard")
          .hasNumberOfSteps(2)
          .step(1) { step ->
            step.hasTitle("Book course before December 2023")
          }
          .step(2) { step ->
            step.hasTitle("Attend course before March 2024")
          }
          .wasCreatedAtPrison("BXI")
          .wasUpdatedAtPrison("MDI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedBy("buser_gen")
          .hasNoGoalNote()
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline.events.size).isEqualTo(7)
      assertThat(timeline)
        .anyOfEventNumber(5, 6) {
          // either the 5th or 6th Timeline event will be the GOAL_UPDATED event
          it.hasEventType(TimelineEventType.GOAL_UPDATED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = createCaptor<Map<String, String>>()

      verify(telemetryClient).trackEvent(
        eq("goal-updated"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      verify(telemetryClient).trackEvent(
        eq("step-removed"),
        capture(eventPropertiesCaptor),
        isNull(),
      )

      val goalUpdatedEventProperties = eventPropertiesCaptor.firstValue
      val stepRemovedEventProperties = eventPropertiesCaptor.secondValue
      assertThat(goalUpdatedEventProperties)
        .containsEntry("reference", goalReference.toString())
      assertThat(stepRemovedEventProperties)
        .containsEntry("reference", goalReference.toString())
        .containsEntry("stepCount", "2")
      assertThat(goalUpdatedEventProperties["correlationId"])
        .isEqualTo(stepRemovedEventProperties["correlationId"])
    }
  }

  @Test
  fun `should update goal given the goal fields are unchanged and the only change is to add a step`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequest())
    createActionPlan(
      username = "auser_gen",
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(
        goals = listOf(
          aValidCreateGoalRequest(
            steps = listOf(
              aValidCreateStepRequest(title = "Book course"),
            ),
          ),
        ),
      ),
    )

    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference
    val step1 = goal.steps[0]

    val newStep = aValidUpdateStepRequest(
      stepReference = null,
      title = "Attend course before March 2024",
      sequenceNumber = 2,
    )

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goal.goalReference,
      title = goal.title,
      targetCompletionDate = goal.targetCompletionDate,
      notes = "Updated goal text",
      prisonId = goal.createdAtPrison,
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = step1.stepReference,
          title = step1.title,
          status = step1.status,
          sequenceNumber = step1.sequenceNumber,
        ),
        // the New Step is the only thing that is changed in the request - add a new Step
        newStep,
      ),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          username = "buser_gen",
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()

    // Then
    val actual = getActionPlan(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasNumberOfSteps(2)
          .step(1) { step ->
            step
              .hasTitle(step1.title)
              .hasStatus(step1.status)
              .hasReference(step1.stepReference)
          }
          .step(2) { step ->
            step
              .hasTitle(newStep.title)
              .hasStatus(newStep.status)
          }
          .wasCreatedAtPrison("BXI")
          .wasUpdatedAtPrison("BXI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedBy("buser_gen")
          .hasGoalNote("Updated goal text")
      }

    val timeline = getTimeline(prisonNumber)
    assertThat(timeline)
      .event(5) {
        // the 5th Timeline event will be the GOAL_UPDATED event
        it.hasEventType(TimelineEventType.GOAL_UPDATED)
          .wasActionedBy("buser_gen")
          .hasActionedByDisplayName("Bernie User")
      }

    val notes = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(
      actual.goals[0].goalReference,
      EntityType.GOAL,
      NoteType.GOAL,
    )
    assertThat(notes.size).isGreaterThan(0)
    assertThat(notes[0].content).isEqualTo("Updated goal text")

    // Currently no telemetry events are sent for when Steps are added/edited but no changes to the parent Goal
    // The only telemetry events sent are GOAL_UPDATED (which does not cover this scenario) or STEP_REMOVED
  }
}
