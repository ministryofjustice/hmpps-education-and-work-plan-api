package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest

class GetGoalsTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
  }

  private val prisonNumber = aValidPrisonNumber()
  private val archivedGoal = "Learn French"
  private val prisonerGoals = listOf(archivedGoal, "Pass Health and Safety City & Guilds", "Get a black belt")

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return 404 if no goals exist for prisoner yet`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should return goals with view only role`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val getGoalsResponse = webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)
      .responseBody.collectList().block()!!.toList()

    // Then
    assertThat(getGoalsResponse).hasSize(3)
    assertThat(getGoalsResponse.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `should return goals with edit role`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val getGoalsResponse = webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)
      .responseBody.collectList().block()!!.toList()

    // Then
    assertThat(getGoalsResponse).hasSize(3)
    assertThat(getGoalsResponse.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `Should return only in-progress goals if requested`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val getGoalsResponse = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ACTIVE", aValidPrisonNumber(), aValidReference())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)
      .responseBody.collectList().block()!!.toList()

    // Then
    assertThat(getGoalsResponse).hasSize(2)
    assertThat(getGoalsResponse.map { it.title }).isEqualTo(prisonerGoals - archivedGoal)
  }

  @Test
  fun `Should return only archived goals if requested`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val getGoalsResponse = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED", aValidPrisonNumber(), aValidReference())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)
      .responseBody.collectList().block()!!.toList()

    // Then
    assertThat(getGoalsResponse).hasSize(1)
    assertThat(getGoalsResponse[0].title).isEqualTo(archivedGoal)
  }

  private fun anActionPlanExistsWithAnArchivedGoal(): ActionPlanResponse {
    prisonerGoals.forEach { title ->
      val createGoalRequest = aValidCreateGoalRequest(
        title = title,
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
    }
    val actionPlan = getActionPlan(prisonNumber)
    val goalToArchive = actionPlan.goals.first { it.title == archivedGoal }
    archiveGoal(prisonNumber, aValidArchiveGoalRequest(goalToArchive.goalReference))
    return actionPlan
  }
}
