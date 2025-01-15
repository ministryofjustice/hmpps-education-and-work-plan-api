package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat

class GetGoalsTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
  }

  private val prisonNumber = randomValidPrisonNumber()

  @BeforeEach
  fun createPrisonerActionPlanAndGoals() {
    createActionPlan(
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(
        goals = listOf(
          aValidCreateGoalRequest(title = "Goal 1", notes = null),
          aValidCreateGoalRequest(title = "Goal 2", notes = "Only goal 2 has a goal note"),
          aValidCreateGoalRequest(title = "Goal 3", notes = null),
          aValidCreateGoalRequest(title = "Goal 4", notes = null),
          aValidCreateGoalRequest(title = "Goal 5", notes = null),
        ),
      ),
      testSensitiveToGoalCreationOrder = true,
    )
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, aValidReference())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return 404 if no goals at all exist for prisoner yet`() {
    // Given
    val prisonNumberWithNoGoals = "Z9999AZ"

    // When
    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumberWithNoGoals, aValidReference())
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
  fun `should return goals given prisoner has goals`() {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(5)
      .allGoals {
        it.hasStatus(GoalStatus.ACTIVE)
      }
      .goal(1) {
        it.hasTitle("Goal 1")
      }
      .goal(2) {
        it.hasTitle("Goal 2")
      }
      .goal(3) {
        it.hasTitle("Goal 3")
      }
      .goal(4) {
        it.hasTitle("Goal 4")
      }
      .goal(5) {
        it.hasTitle("Goal 5")
      }
  }

  @Test
  fun `should return goals including mapping any notes`() {
    // Given
    val prisonerGoals = getActionPlan(prisonNumber).goals
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 3" }.goalReference,
        note = "Goal 3 archive note",
      ),
    )
    completeGoal(
      prisonNumber = prisonNumber,
      completeGoalRequest = aValidCompleteGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 2" }.goalReference,
        note = "Goal 2 completion note",
      ),
    )

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(5)
      .goal(1) {
        it.hasTitle("Goal 1")
          .hasStatus(GoalStatus.ACTIVE)
          .hasNoNotes()
      }
      .goal(2) {
        it.hasTitle("Goal 2")
          .hasStatus(GoalStatus.COMPLETED)
          .hasGoalNote("Only goal 2 has a goal note")
          .hasNoArchiveNote()
          .hasCompletedNote("Goal 2 completion note")
      }
      .goal(3) {
        it.hasTitle("Goal 3")
          .hasStatus(GoalStatus.ARCHIVED)
          .hasNoGoalNote()
          .hasArchiveNote("Goal 3 archive note")
          .hasNoCompletedNote()
      }
      .goal(4) {
        it.hasTitle("Goal 4")
          .hasStatus(GoalStatus.ACTIVE)
          .hasNoNotes()
      }
      .goal(5) {
        it.hasTitle("Goal 5")
          .hasStatus(GoalStatus.ACTIVE)
          .hasNoNotes()
      }
  }

  @Test
  fun `Should return only in-progress goals if requested`() {
    // Given
    val prisonerGoals = getActionPlan(prisonNumber).goals
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 3" }.goalReference,
      ),
    )
    completeGoal(
      prisonNumber = prisonNumber,
      completeGoalRequest = aValidCompleteGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 4" }.goalReference,
      ),
    )

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ACTIVE", prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(3)
      .allGoals {
        it.hasStatus(GoalStatus.ACTIVE)
      }
      .goal(1) {
        it.hasTitle("Goal 1")
      }
      .goal(2) {
        it.hasTitle("Goal 2")
      }
      .goal(3) {
        it.hasTitle("Goal 5")
      }
  }

  @Test
  fun `Should return only archived goals if requested`() {
    // Given
    val prisonerGoals = getActionPlan(prisonNumber).goals
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 2" }.goalReference,
      ),
    )
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 4" }.goalReference,
      ),
    )

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED", prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(2)
      .allGoals {
        it.hasStatus(GoalStatus.ARCHIVED)
      }
      .goal(1) {
        it.hasTitle("Goal 2")
      }
      .goal(2) {
        it.hasTitle("Goal 4")
      }
  }

  @Test
  fun `Should return archived and in-progress goals if requested with a comma delimited list`() {
    // Given
    val prisonerGoals = getActionPlan(prisonNumber).goals
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 4" }.goalReference,
      ),
    )
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 5" }.goalReference,
      ),
    )
    completeGoal(
      prisonNumber = prisonNumber,
      completeGoalRequest = aValidCompleteGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 3" }.goalReference,
      ),
    )

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED,ACTIVE", prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(4)
      .goal(1) {
        it.hasTitle("Goal 1").hasStatus(GoalStatus.ACTIVE)
      }
      .goal(2) {
        it.hasTitle("Goal 2").hasStatus(GoalStatus.ACTIVE)
      }
      .goal(3) {
        it.hasTitle("Goal 4").hasStatus(GoalStatus.ARCHIVED)
      }
      .goal(4) {
        it.hasTitle("Goal 5").hasStatus(GoalStatus.ARCHIVED)
      }
  }

  @Test
  fun `Should return archived and in-progress goals if requested with multiple status query string parameters`() {
    // Given
    val prisonerGoals = getActionPlan(prisonNumber).goals
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 4" }.goalReference,
      ),
    )
    archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalRequest = aValidArchiveGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 5" }.goalReference,
      ),
    )
    completeGoal(
      prisonNumber = prisonNumber,
      completeGoalRequest = aValidCompleteGoalRequest(
        goalReference = prisonerGoals.first { it.title == "Goal 3" }.goalReference,
      ),
    )

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=ARCHIVED&status=ACTIVE", prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(4)
      .goal(1) {
        it.hasTitle("Goal 1").hasStatus(GoalStatus.ACTIVE)
      }
      .goal(2) {
        it.hasTitle("Goal 2").hasStatus(GoalStatus.ACTIVE)
      }
      .goal(3) {
        it.hasTitle("Goal 4").hasStatus(GoalStatus.ARCHIVED)
      }
      .goal(4) {
        it.hasTitle("Goal 5").hasStatus(GoalStatus.ARCHIVED)
      }
  }

  @Test
  fun `Should return 200 response with empty goals collection given prisoner has goals but none match the status filter`() {
    // Given

    // When
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?status=COMPLETED", prisonNumber, aValidReference())
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
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasNumberOfGoals(0)
  }

  @Test
  fun `Should return 400 if requested status is not recognised`() {
    webTestClient.get()
      .uri("$URI_TEMPLATE?status=FOO", prisonNumber, aValidReference())
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
}
