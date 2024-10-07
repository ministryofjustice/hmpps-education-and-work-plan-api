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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.util.*

class UnarchiveGoalTest : IntegrationTestBase() {

  companion object {
    private const val UNARCHIVE_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}/unarchive"
    private const val ARCHIVE_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}/archive"
  }

  private val prisonNumber = aValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(UNARCHIVE_URI_TEMPLATE, prisonNumber, aValidReference())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should require the edit role`() {
    webTestClient.put()
      .uri(UNARCHIVE_URI_TEMPLATE, prisonNumber, aValidReference())
      .withBody(aValidArchiveGoalRequest())
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
  fun `should return 204 and unarchive a goal and reset the reason and other text `() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val reasonOther = "Because it's Monday"
    val archiveRequestWithOtherReason = aValidArchiveGoalRequest(
      goalReference = goalReference,
      reason = ReasonToArchiveGoal.OTHER,
      reasonOther = "Other reason",
    )
    archiveAGoal(prisonNumber, goalReference, archiveRequestWithOtherReason)
    val unarchiveGoalRequest = aValidUnarchiveGoalRequest(goalReference)
    // when
    unarchiveAGoal(prisonNumber, goalReference, unarchiveGoalRequest)
      .expectStatus()
      .isNoContent()

    // then
    assertThat(getActionPlan(prisonNumber))
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasStatus(GoalStatus.ACTIVE)
          .hasArchiveReason(null)
          .hasArchiveReasonOther(null)
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(4) { // the 4th Timeline event will be the GOAL_UNARCHIVED event
          it.hasEventType(TimelineEventType.GOAL_UNARCHIVED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-unarchived"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalArchivedEventProperties = eventPropertiesCaptor.firstValue
      assertThat(goalArchivedEventProperties)
        .containsEntry("reference", goalReference.toString())
    }
  }

  @Test
  fun `should return 204 and unarchive a goal and reset the reason and other text and archive note is deleted `() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val reasonOther = "Because it's Monday"
    val archiveNote = "an archive note"
    val archiveRequestWithOtherReason = aValidArchiveGoalRequest(
      goalReference = goalReference,
      reason = ReasonToArchiveGoal.OTHER,
      reasonOther = "Other reason",
      note = archiveNote,
    )
    archiveAGoal(prisonNumber, goalReference, archiveRequestWithOtherReason)

    // check that the note is there:

    val noteBefore = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(goalReference, EntityType.GOAL, NoteType.GOAL_ARCHIVAL).firstOrNull()
    assertThat(noteBefore!!.content).isEqualTo(archiveNote)

    val unarchiveGoalRequest = aValidUnarchiveGoalRequest(goalReference)
    // when
    unarchiveAGoal(prisonNumber, goalReference, unarchiveGoalRequest)
      .expectStatus()
      .isNoContent()

    // then
    assertThat(getActionPlan(prisonNumber))
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasStatus(GoalStatus.ACTIVE)
          .hasArchiveReason(null)
          .hasArchiveReasonOther(null)
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(4) { // the 4th Timeline event will be the GOAL_UNARCHIVED event
          it.hasEventType(TimelineEventType.GOAL_UNARCHIVED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-unarchived"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalArchivedEventProperties = eventPropertiesCaptor.firstValue
      assertThat(goalArchivedEventProperties)
        .containsEntry("reference", goalReference.toString())
    }

    // check that the note has been deleted
    val noteAfter = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(goalReference, EntityType.GOAL, NoteType.GOAL_ARCHIVAL).firstOrNull()
    assertThat(noteAfter).isNull()
  }

  @Test
  fun `should return 404 if the goal isn't found`() {
    // given
    val unarchiveGoalRequest = aValidUnarchiveGoalRequest()
    val goalReference = unarchiveGoalRequest.goalReference

    // when
    val response = unarchiveAGoal(prisonNumber, goalReference, unarchiveGoalRequest)
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
    val unarchiveGoalRequest = aValidUnarchiveGoalRequest(goalReference)
    val aDifferentPrisonNumber = "Z9876YX"

    // when
    val response = unarchiveAGoal(aDifferentPrisonNumber, goalReference, unarchiveGoalRequest)
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
      .uri(UNARCHIVE_URI_TEMPLATE, prisonNumber, goalReference)
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
  fun `should return 409 if goal is not archived`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val archiveGoalRequest = aValidUnarchiveGoalRequest(goalReference)

    // when
    val response = unarchiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
      .expectStatus()
      .isEqualTo(HttpStatus.CONFLICT)
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.CONFLICT.value())
      .hasUserMessageContaining("Could not unarchive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [ACTIVE] that can't be unarchived")
  }

  private fun unarchiveAGoal(
    prisonNumber: String,
    goalReference: UUID,
    unarchiveGoalRequest: UnarchiveGoalRequest,
  ): WebTestClient.ResponseSpec = webTestClient.put()
    .uri(UNARCHIVE_URI_TEMPLATE, prisonNumber, goalReference)
    .withBody(unarchiveGoalRequest)
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

  private fun archiveAGoal(
    prisonNumber: String,
    goalReference: UUID,
    archiveGoalRequest: ArchiveGoalRequest,
  ): WebTestClient.ResponseSpec = webTestClient.put()
    .uri(ARCHIVE_URI_TEMPLATE, prisonNumber, goalReference)
    .withBody(archiveGoalRequest)
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
    .expectStatus()
    .isNoContent()

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
