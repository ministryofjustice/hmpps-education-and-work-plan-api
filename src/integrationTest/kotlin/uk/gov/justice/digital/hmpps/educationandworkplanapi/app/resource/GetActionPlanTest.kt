package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class GetActionPlanTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}"
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
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/action-plans/$prisonNumber")
  }

  @Test
  fun `should not get action plan given prisoner has no action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("ActionPlan for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should get action plan for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest()
    createActionPlan(prisonNumber, createActionPlanRequest)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .goal(1) {
        it.wasCreatedAtPrison("BXI")
          .wasCreatedBy("auser_gen")
          .hasCreatedByDisplayName("Albert User")
          .wasUpdatedAtPrison("BXI")
          .wasUpdatedBy("auser_gen")
          .hasUpdatedByDisplayName("Albert User")
          .hasGoalNote("Chris would like to improve his listening skills, not just his verbal communication")
          .hasNoArchiveNote()
      }
  }

  @Test
  fun `should get action plan with multiple goals in order`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest(
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)
    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn French"))
    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn Spanish"))

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .goal(1) {
        it.hasTitle("Learn Spanish")
      }
      // verify order of remaining goals
      .goal(2) {
        it.hasTitle("Learn French")
      }
      .goal(3) {
        it.hasTitle("Learn German")
      }
  }

  private fun createGoal(prisonNumber: String, createGoalRequest: CreateGoalRequest) {
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))
    webTestClient.post()
      .uri("$URI_TEMPLATE/goals", prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }
}
