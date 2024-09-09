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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.util.*

class ArchiveGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}/archive"
  }

  private val prisonNumber = aValidPrisonNumber()

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
  fun `should return 204 and archive a goal and record the reason and other text `() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val reasonOther = "Because it's Monday"
    val archiveGoalRequest = aValidArchiveGoalRequest(
      goalReference = goalReference,
      ReasonToArchiveGoal.OTHER,
      reasonOther,
    )

    // when
    archiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
      .expectStatus()
      .isNoContent()

    // then
    assertThat(getActionPlan(prisonNumber))
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(1) { goal ->
        goal
          .hasStatus(GoalStatus.ARCHIVED)
          .hasArchiveReason(ReasonToArchiveGoal.OTHER)
          .hasArchiveReasonOther(reasonOther)
      }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(3) { // the 3rd Timeline event will be the GOAL_ARCHIVED event
          it.hasEventType(TimelineEventType.GOAL_ARCHIVED)
            .wasActionedBy("buser_gen")
            .hasActionedByDisplayName("Bernie User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-archived"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalArchivedEventProperties = eventPropertiesCaptor.firstValue
      assertThat(goalArchivedEventProperties)
        .containsEntry("reference", goalReference.toString())
    }
  }

  @Test
  fun `should return 404 if the goal isn't found`() {
    // given
    val archiveGoalRequest = aValidArchiveGoalRequest()
    val goalReference = archiveGoalRequest.goalReference

    // when
    val response = archiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
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
    val archiveGoalRequest = aValidArchiveGoalRequest(goalReference)
    val aDifferentPrisonNumber = "Z9876YX"

    // when
    val response = archiveAGoal(aDifferentPrisonNumber, goalReference, archiveGoalRequest)
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
  fun `should return 400 if other reason without description`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val archiveGoalRequest = aValidArchiveGoalRequest(
      goalReference = goalReference,
      reason = ReasonToArchiveGoal.OTHER,
      reasonOther = null,
    )

    // when
    val response = archiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
      .expectStatus()
      .is4xxClientError
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Archive reason is OTHER but no description provided")
  }

  @Test
  fun `should return 409 if goal is already archived`() {
    // given
    val goalReference = createAGoalAndGetTheReference(prisonNumber)
    val archiveGoalRequest = aValidArchiveGoalRequest(goalReference = goalReference)
    archiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
      .expectStatus()
      .isNoContent

    // when
    val response = archiveAGoal(prisonNumber, goalReference, archiveGoalRequest)
      .expectStatus()
      .isEqualTo(HttpStatus.CONFLICT)
      .returnResult(ErrorResponse::class.java)

    // then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.CONFLICT.value())
      .hasUserMessageContaining("Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [ARCHIVED] that can't be archived")
  }

  private fun archiveAGoal(
    prisonNumber: String,
    goalReference: UUID,
    archiveGoalRequest: ArchiveGoalRequest,
  ): WebTestClient.ResponseSpec = webTestClient.put()
    .uri(URI_TEMPLATE, prisonNumber, goalReference)
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
