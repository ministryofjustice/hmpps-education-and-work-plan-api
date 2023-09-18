package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
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
      .withBody(aValidCreateGoalRequest())
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
      .withBody(createRequest)
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
      .hasUserMessage("Validation failed for object='createGoalRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Steps cannot be empty when creating a Goal")
  }

  @Test
  fun `should fail to create goal given null fields`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
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
      .hasUserMessageContaining("value failed for JSON property title due to missing (therefore NULL) value for creator parameter title")
  }

  @Test
  @Transactional
  fun `should add goal and create a new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val stepRequest = aValidCreateStepRequest(targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS)
    val createGoalRequest = aValidCreateGoalRequest(steps = listOf(stepRequest), notes = "Notes about the goal...")

    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createGoalRequest)
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
      .wasCreatedAtPrison(createGoalRequest.prisonId)
      .wasCreatedBy(dpsUsername)
      .hasCreatedByDisplayName(displayName)
      .wasUpdatedAtPrison(createGoalRequest.prisonId)
      .wasUpdatedBy(dpsUsername)
      .hasUpdatedByDisplayName(displayName)
    val step = goal.steps!![0]
    assertThat(step)
      .hasTitle(stepRequest.title)
      .hasTargetDateRange(TargetDateRangeEntity.ZERO_TO_THREE_MONTHS)
      .hasStatus(StepStatus.NOT_STARTED)
      .wasCreatedBy(dpsUsername)

    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "1",
      "reference" to goal.reference.toString(),
      "notesCharacterCount" to "23",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }

    // assert timeline event is created successfully
    val prisonerTimeline = timelineRepository.findByPrisonNumber(prisonNumber)!!
    assertThat(prisonerTimeline.prisonNumber).isEqualTo(prisonNumber)
    val events = prisonerTimeline.events!!
    assertThat(events.size).isEqualTo(1)
    assertThat(events[0]).hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
    assertThat(events[0]).hasSourceReference(actionPlan.reference.toString())
    assertThat(events[0]).hasNoContextualInfo()
    assertThat(events[0]).hasAReference()
    assertThat(events[0]).hasJpaManagedFieldsPopulated()
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
    val createRequest = aValidCreateGoalRequest(
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
    )

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
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

    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "2",
      "reference" to goal.reference.toString(),
      "notesCharacterCount" to "83",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }

    // assert timeline event is created successfully
    val prisonerTimeline = timelineRepository.findByPrisonNumber(prisonNumber)!!
    assertThat(prisonerTimeline.prisonNumber).isEqualTo(prisonNumber)
    val events = prisonerTimeline.events!!
    assertThat(events.size).isEqualTo(1)
    assertThat(events[0]).hasEventType(TimelineEventType.GOAL_CREATED)
    assertThat(events[0]).hasSourceReference(goal.reference.toString())
    assertThat(events[0]).hasContextualInfo(goal.title!!)
    assertThat(events[0]).hasAReference()
    assertThat(events[0]).hasJpaManagedFieldsPopulated()
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
      .withBody(createRequest)
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

    val expectedEventCustomDimensions = mapOf(
      "status" to "ACTIVE",
      "stepCount" to "2",
      "reference" to goal.reference.toString(),
      "notesCharacterCount" to "0",
    )
    await.untilAsserted {
      verify(telemetryClient).trackEvent("goal-create", expectedEventCustomDimensions, null)
    }
  }
}
