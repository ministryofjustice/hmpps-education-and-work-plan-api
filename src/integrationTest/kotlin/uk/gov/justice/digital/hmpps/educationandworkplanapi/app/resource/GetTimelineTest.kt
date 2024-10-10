package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.timelineValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonPeriod
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidSignificantMovementAdmission
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidTransferDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class GetTimelineTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/timelines/{prisonNumber}"
    private const val CREATE_GOALS_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
    private const val UPDATE_GOAL_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/timelines/$prisonNumber")
  }

  @Test
  fun `should not get timeline given prisoner has no timeline`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineNotFound(prisonNumber)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          TIMELINE_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Timeline not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should get timeline for prisoner`() {
    // Given
    val prisonNumber = timelineValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("MDI", "BXI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "MDI"),
            ),
            transfers = listOf(
              aValidTransferDetail(fromPrisonId = "MDI", toPrisonId = "BXI"),
            ),
          ),
        ),
      ),
    )
    val createActionPlanRequest = aValidCreateActionPlanRequest(
      reviewDate = LocalDate.now(),
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)

    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]

    // When
    await.untilAsserted {
      val response = webTestClient.get()
        .uri(URI_TEMPLATE, prisonNumber)
        .bearerToken(
          aValidTokenWithAuthority(
            TIMELINE_RO,
            privateKey = keyPair.private,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(TimelineResponse::class.java)

      // Then
      val actual = response.responseBody.blockFirst()!!
      val actionPlanCreatedCorrelationId = actual.events[3].correlationId
      assertThat(actual)
        .isForPrisonNumber(prisonNumber)
        .hasNumberOfEvents(4)
        .event(1) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_ADMISSION)
            .hasPrisonId("MDI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasNoContextualInfo()
        }
        .event(2) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_TRANSFER)
            .hasPrisonId("BXI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasContextualInfo(mapOf("PRISON_TRANSFERRED_FROM" to "MDI"))
        }
        .event(3) {
          it.hasSourceReference(actionPlan.reference.toString())
            .hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
            .hasNoContextualInfo()
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .event(4) {
          it.hasSourceReference(goal.goalReference.toString())
            .hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
            .hasContextualInfo(mapOf("GOAL_TITLE" to goal.title))
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
    }
  }

  @Test
  fun `should get timeline with multiple events in order`() {
    // Given
    val prisonNumber = timelineValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("MDI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "MDI"),
            ),
            transfers = emptyList(),
          ),
        ),
      ),
    )
    createInduction(prisonNumber, aValidCreateInductionRequest())
    val createActionPlanRequest = aValidCreateActionPlanRequest(
      reviewDate = LocalDate.now(),
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)
    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn French"))

    val induction = getInduction(prisonNumber)
    val actionPlan = getActionPlan(prisonNumber)
    val actionPlanReference = actionPlan.reference
    val goal1Reference = actionPlan.goals[1].goalReference // The Action Plan returned by the API has Goals in reverse chronological order. The first Goal created is the 2nd in the list
    val goal2Reference = actionPlan.goals[0].goalReference // and the 2nd Goal created is the first in the list.
    val stepToUpdate = actionPlan.goals[1].steps[0]

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goal1Reference,
      title = "Learn Spanish",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepToUpdate.stepReference,
          title = "Research course options",
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )
    updateGoal(prisonNumber, updateGoalRequest)

    // When
    await.untilAsserted {
      val response = webTestClient.get()
        .uri(URI_TEMPLATE, prisonNumber)
        .bearerToken(
          aValidTokenWithAuthority(
            TIMELINE_RO,
            privateKey = keyPair.private,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(TimelineResponse::class.java)

      // Then
      val actual = response.responseBody.blockFirst()!!
      val actionPlanCreatedCorrelationId = actual.events[2].correlationId
      val goalUpdatedCorrelationId = actual.events[6].correlationId
      assertThat(actual)
        .isForPrisonNumber(prisonNumber)
        .hasNumberOfEvents(8)
        .event(1) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_ADMISSION)
            .hasPrisonId("MDI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasNoContextualInfo()
        }
        .event(2) {
          it.hasEventType(TimelineEventType.INDUCTION_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(induction.reference.toString())
            .hasNoContextualInfo() // creating an induction has no contextual info
            .correlationIdIsNotEqualTo(actionPlanCreatedCorrelationId)
            .correlationIdIsNotEqualTo(goalUpdatedCorrelationId)
        }
        .event(3) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(actionPlanReference.toString())
            .hasNoContextualInfo() // creating an action plan has no contextual info
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .event(4) {
          it.hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal1Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn German"))
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .event(5) {
          it.hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal2Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn French"))
            .correlationIdIsNotEqualTo(actionPlanCreatedCorrelationId)
            .correlationIdIsNotEqualTo(goalUpdatedCorrelationId)
        }
        .event(6) {
          it.hasEventType(TimelineEventType.GOAL_UPDATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal1Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn Spanish")) // Learn German changed to Learn Spanish
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
        .event(7) {
          it.hasEventType(TimelineEventType.STEP_STARTED)
            .hasPrisonId("BXI")
            .hasSourceReference(stepToUpdate.stepReference.toString())
            .hasContextualInfo(mapOf("STEP_TITLE" to "Research course options"))
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
        .event(8) {
          it.hasEventType(TimelineEventType.STEP_UPDATED)
            .hasPrisonId("BXI")
            .hasSourceReference(stepToUpdate.stepReference.toString())
            .hasContextualInfo(mapOf("STEP_TITLE" to "Research course options"))
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
    }
  }

  @Test
  fun `should get timeline for prisoner with no plp events`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("BXI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "BXI"),
            ),
            transfers = emptyList(),
          ),
        ),
      ),
    )

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          TIMELINE_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(TimelineResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfEvents(1)
      .event(1) {
        it.hasSourceReference("1")
          .hasEventType(TimelineEventType.PRISON_ADMISSION)
          .hasPrisonId("BXI")
          .wasActionedBy("system")
          .hasNoActionedByDisplayName()
          .hasNoContextualInfo()
      }
  }

  private fun createGoal(prisonNumber: String, createGoalRequest: CreateGoalRequest) {
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  private fun updateGoal(prisonNumber: String, updateGoalRequest: UpdateGoalRequest) {
    webTestClient.put()
      .uri(UPDATE_GOAL_URI_TEMPLATE, prisonNumber, updateGoalRequest.goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()
  }
}
