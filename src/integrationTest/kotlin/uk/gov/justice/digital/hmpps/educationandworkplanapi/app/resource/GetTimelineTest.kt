package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class GetTimelineTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/timelines/{prisonNumber}"
    private const val CREATE_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    private const val CREATE_GOAL_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
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

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
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
  @Transactional
  fun `should get timeline for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest(
      reviewDate = LocalDate.now(),
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(TimelineResponse::class.java)

    // Then
    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .event(0) {
        it.hasSourceReference(actionPlan!!.reference.toString())
          .hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
          .hasPrisonId("BXI")
          .wasActionedBy("auser_gen")
          .hasActionedByDisplayName("Albert User")
          .hasNoContextualInfo()
      }
  }

  @Test
  @Transactional
  fun `should get timeline with multiple events in order`() {
    // Given
    val prisonNumber = anotherValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest(
      reviewDate = LocalDate.now(),
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)
    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn French"))

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    val actionPlanReference = actionPlan!!.reference!!
    val goal1Reference = actionPlan.goals!![0].reference!!
    val goal2Reference = actionPlan.goals!![1].reference!!
    updateGoal(prisonNumber, aValidUpdateGoalRequest(goalReference = goal1Reference, title = "Learn Spanish"))

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(TimelineResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .event(0) {
        it.hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
          .hasPrisonId("BXI")
          .hasSourceReference(actionPlanReference.toString())
          .hasNoContextualInfo() // creating an action plan has no contextual info
      }
      .event(1) {
        it.hasEventType(TimelineEventType.GOAL_CREATED)
          .hasPrisonId("BXI")
          .hasSourceReference(goal2Reference.toString())
          .hasContextualInfo("Learn French")
      }
      .event(2) {
        it.hasEventType(TimelineEventType.GOAL_UPDATED)
          .hasPrisonId("BXI")
          .hasSourceReference(goal1Reference.toString())
          .hasContextualInfo("Learn Spanish") // Learn German changed to Learn Spanish
      }
  }

  private fun createActionPlan(prisonNumber: String, createActionPlanRequest: CreateActionPlanRequest) {
    webTestClient.post()
      .uri(CREATE_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  private fun createGoal(prisonNumber: String, createGoalRequest: CreateGoalRequest) {
    webTestClient.post()
      .uri(CREATE_GOAL_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  private fun updateGoal(prisonNumber: String, updateGoalRequest: UpdateGoalRequest) {
    webTestClient.put()
      .uri(UPDATE_GOAL_URI_TEMPLATE, prisonNumber, updateGoalRequest.goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()
  }
}
