package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat

class GetGoalTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
    private val prisonNumber = randomValidPrisonNumber()
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    val goalReference = aValidReference()

    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // Given
    val goalReference = aValidReference()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
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
      .hasDeveloperMessage("Access denied on uri=/action-plans/$prisonNumber/goals/$goalReference")
  }

  @Test
  fun `should return not found given goal does not exist`() {
    // Given
    val goalReference = aValidReference()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
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
      .hasUserMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should return not found given goal exists but for a different prisoner`() {
    // Given
    val someOtherPrisonNumber = randomValidPrisonNumber()
    createActionPlan(
      prisonNumber = someOtherPrisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(),
    )
    val actionPlan = getActionPlan(someOtherPrisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
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
      .hasUserMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should return goal given prison number and goal reference`() {
    // Given
    createActionPlan(
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(),
    )
    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasReference(goalReference)
      // archive note should be null.
      .hasNoArchiveNote()
  }

  @Test
  fun `should return goal given prison number with archive note`() {
    // Given a goal is created and archived.
    createActionPlan(
      prisonNumber = prisonNumber,
      createActionPlanRequest = aValidCreateActionPlanRequest(),
    )
    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val goalReference = goal.goalReference

    val reasonOther = "Because it's Monday"
    val archiveNote = "an archive note"
    val archiveGoalRequest = aValidArchiveGoalRequest(
      goalReference = goalReference,
      ReasonToArchiveGoal.OTHER,
      reasonOther,
      note = archiveNote,
    )

    archiveGoal(prisonNumber, archiveGoalRequest)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(GoalResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasReference(goalReference)
    // archive note should not be null.
    assertThat(actual).hasArchiveNote(archiveNote)
  }
}
