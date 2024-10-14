package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.util.*

class CompleteGoalTest : IntegrationTestBase() {

  companion object {
    const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}/complete"
  }

  private val prisonNumber = anotherValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, aValidReference())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should require the edit role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, aValidReference())
      .withBody(aValidArchiveGoalRequest())
      .bearerToken(aValidTokenWithAuthority(GOALS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 204 and complete a goal`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val completeGoalRequest = aValidCompleteGoalRequest(
      goalReference = goalReference,
    )
    // when
    completeAGoal(prisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isNoContent()

    // then
    assertThat(getActionPlan(prisonNumber))
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasStatus(GoalStatus.COMPLETED)
          .hasNoCompletedNote()
          .hasCompletedSteps()
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(5) {
          it.hasEventType(TimelineEventType.GOAL_COMPLETED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-completed"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalCompleteEventProperties = eventPropertiesCaptor.firstValue
      assertThat(goalCompleteEventProperties)
        .containsEntry("reference", goalReference.toString())
    }
  }

  @Test
  fun `should return 204 and complete a goal and create an completion note`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val noteText = "Completed the goal! "
    val completeGoalRequest = aValidCompleteGoalRequest(
      goalReference = goalReference,
      note = noteText,
    )
    // when
    completeAGoal(prisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isNoContent()

    // then
    assertThat(getActionPlan(prisonNumber))
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasStatus(GoalStatus.COMPLETED)
          .hasCompletedNote(noteText)
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(5) { // the 3rd Timeline event will be the GOAL_ARCHIVED event
          it.hasEventType(TimelineEventType.GOAL_COMPLETED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-completed"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalArchivedEventProperties = eventPropertiesCaptor.firstValue
      assertThat(goalArchivedEventProperties)
        .containsEntry("reference", goalReference.toString())

      val note = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(
        goalReference,
        EntityType.GOAL,
        NoteType.GOAL_COMPLETION,
      ).firstOrNull()
      assertThat(note!!.content).isEqualTo(noteText)
    }
  }

  @Test
  fun `should return 404 if the goal isn't found`() {
    // given
    val completeGoalRequest = aValidCompleteGoalRequest()
    val goalReference = completeGoalRequest.goalReference

    // when
    val response = completeAGoal(prisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should return 404 if the goal is for a different prisoner`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val completeGoalRequest = aValidCompleteGoalRequest(goalReference)
    val aDifferentPrisonNumber = "Z9876YX"

    // when
    val response = completeAGoal(aDifferentPrisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Goal with reference [$goalReference] for prisoner [$aDifferentPrisonNumber] not found")
  }

  @Test
  fun `should return 400 if request is malformed`() {
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
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
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property goalReference due to missing (therefore NULL) value for creator parameter goalReference")
  }

  @Test
  fun `should return 409 if goal is already completed`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val completeGoalRequest = aValidCompleteGoalRequest(goalReference = goalReference)
    completeAGoal(prisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isNoContent()

    // when
    val response = completeAGoal(prisonNumber, goalReference, completeGoalRequest)
      .expectStatus()
      .isEqualTo(HttpStatus.CONFLICT)
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.CONFLICT.value())
      .hasUserMessageContaining("Could not complete goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [COMPLETED] that can't be completed")
  }

  private fun completeAGoal(
    prisonNumber: String,
    goalReference: UUID,
    completeGoalRequest: CompleteGoalRequest,
  ): WebTestClient.ResponseSpec = webTestClient.put()
    .uri(URI_TEMPLATE, prisonNumber, goalReference)
    .withBody(completeGoalRequest)
    .bearerToken(
      aValidTokenWithAuthority(
        GOALS_RW,
        username = "buser_gen",
        displayName = "Bernie User",
        privateKey = keyPair.private,
      ),
    )
    .contentType(APPLICATION_JSON)
    .exchange()

  private fun createAGoalAndGetTheReference(prisonNumber: String): UUID {
    val createGoalRequest = aValidCreateGoalRequest(
      title = "Learn French",
      steps = listOf(
        aValidCreateStepRequest(
          title = "Book course",
        ),
        aValidCreateStepRequest(
          title = "Attend course",
        ),
      ),
    )
    createGoal(
      username = "auser_gen",
      displayName = "Albert User",
      prisonNumber = prisonNumber,
      createGoalRequest = createGoalRequest,
    )
    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference
    return goalReference
  }
}
