package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TargetDateRange as TargetDateRangeEntity

class CreateGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .body(Mono.just(aValidCreateGoalRequest()), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create goal given no steps provided`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateGoalRequest(steps = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("At least one Step is required.")
  }

  @Test
  @Transactional
  fun `should add goal and create a new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createStepRequest = aValidCreateStepRequest(targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS)
    val createGoalRequest = aValidCreateGoalRequest(steps = listOf(createStepRequest))

    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createGoalRequest), CreateGoalRequest::class.java)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .wasCreatedBy(dpsUsername)
    val goal = actionPlan!!.goals!![0]
    assertThat(goal)
      .hasTitle(createGoalRequest.title)
      .hasNumberOfSteps(createGoalRequest.steps.size)
      .wasCreatedBy(dpsUsername)
      .hasCreatedByDisplayName(displayName)
      .wasUpdatedBy(dpsUsername)
      .hasUpdatedByDisplayName(displayName)
    val step = goal.steps!![0]
    assertThat(step)
      .hasTitle(createStepRequest.title)
      .hasTargetDateRange(TargetDateRangeEntity.ZERO_TO_THREE_MONTHS)
      .hasStatus(StepStatus.NOT_STARTED)
      .wasCreatedBy(dpsUsername)
  }

  @Test
  @Transactional
  fun `should add goal to prisoner's existing action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlanEntity(prisonNumber = prisonNumber)
    actionPlanRepository.save(actionPlan)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(actionPlan).hasNumberOfGoals(1)
    val createRequest = aValidCreateGoalRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(2)
  }

  @Test
  @Transactional
  fun `should add goal with only mandatory fields populated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlanEntity(prisonNumber = prisonNumber)
    actionPlanRepository.save(actionPlan)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    assertThat(actionPlan).hasNumberOfGoals(1)
    val createRequest = aValidCreateGoalRequest(notes = null)

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(2)
    val goal = actual!!.goals!![1]
    assertThat(goal)
      .hasTitle(createRequest.title)
      .hasNumberOfSteps(createRequest.steps.size)
    assertThat(goal.notes).isNull()
  }
}
