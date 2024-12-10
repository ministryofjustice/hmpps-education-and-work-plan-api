package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
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
  fun `should return 404 if no goals at all exist for prisoner yet`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
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
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(3)
    assertThat(actual.goals.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `should return goals with edit role`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(3)
    assertThat(actual.goals.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `Should return only in-progress goals if requested`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ACTIVE", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(2)
    assertThat(actual.goals.map { it.title }).isEqualTo(prisonerGoals - archivedGoal)
  }

  @Test
  fun `Should return only archived goals if requested`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(1)
    assertThat(actual.goals[0].title).isEqualTo(archivedGoal)
  }

  @Test
  fun `Should return archived and in-progress goals if requested with a comma delimited list`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED,ACTIVE", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(3)
    assertThat(actual.goals.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `Should return archived and in-progress goals if requested with multiple status query string parameters`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED&status=ACTIVE", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).hasSize(3)
    assertThat(actual.goals.map { it.title }).isEqualTo(prisonerGoals)
  }

  @Test
  fun `Should return 200 response with empty goals collection given prisoner has goals but none match the status filter`() {
    // Given
    anActionPlanExistsWithAnArchivedGoal()

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=COMPLETED", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GetGoalsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.goals).isEmpty()
  }

  @Test
  fun `Should return 400 if requested status is not recognised`() {
    webTestClient.get()
      .uri("$URI_TEMPLATE?status=FOO", aValidPrisonNumber(), aValidReference())
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  private fun anActionPlanExistsWithAnArchivedGoal(): ActionPlanResponse {
    val createGoalsRequests = prisonerGoals.map { title ->
      aValidCreateGoalRequest(
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
    }
    createActionPlan(
      username = "auser_gen",
      displayName = "Albert User",
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(
        goals = createGoalsRequests,
      ),
    )
    val actionPlan = getActionPlan(prisonNumber)
    val goalToArchive = actionPlan.goals.first { it.title == archivedGoal }
    archiveGoal(prisonNumber, aValidArchiveGoalRequest(goalToArchive.goalReference))
    return actionPlan
  }
}
