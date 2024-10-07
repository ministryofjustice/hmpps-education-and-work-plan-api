package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class CreateActionPlanTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}"
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
      .withBody(aValidCreateActionPlanRequest())
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create action plan given no goals provided`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateActionPlanRequest(goals = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Goals cannot be empty when creating an Action Plan")
  }

  @Test
  fun `should fail to create action plan given a goal with no steps provided`() {
    val prisonNumber = aValidPrisonNumber()
    val goalWithNoSteps = aValidCreateGoalRequest(steps = emptyList())
    val createRequest = aValidCreateActionPlanRequest(goals = listOf(goalWithNoSteps))

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Error on field 'goals[0].steps': rejected value [[]], Steps cannot be empty when creating a Goal")
  }

  @Test
  fun `should fail to create action plan given review date is in the past`() {
    val prisonNumber = aValidPrisonNumber()
    val invalidReviewDate = LocalDate.now().minusDays(1)
    val createRequest = aValidCreateActionPlanRequest(reviewDate = invalidReviewDate)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("Validation failed for object='createActionPlanRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Error on field 'reviewDate': rejected value [$invalidReviewDate], Cannot be in the past")
  }

  @Test
  fun `should fail to create action plan given null fields`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
      .hasUserMessageContaining("value failed for JSON property goals due to missing (therefore NULL) value for creator parameter goals")
  }

  @Test
  fun `should fail to create action plan given action plan already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber)

    val createRequest = aValidCreateActionPlanRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("An Action Plan already exists for prisoner $prisonNumber.")
  }

  @Test
  @Transactional
  fun `should create a new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createStepRequest = aValidCreateStepRequest()
    val createGoalRequest = aValidCreateGoalRequest(steps = listOf(createStepRequest))
    val createActionPlanRequest = aValidCreateActionPlanRequest(goals = listOf(createGoalRequest))
    val expectedReviewDate = createActionPlanRequest.reviewDate!!
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
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
      .hasReviewDate(expectedReviewDate)
      .hasNumberOfGoals(1)
      .wasCreatedBy(dpsUsername)
    val goal = actionPlan!!.goals!![0]
    assertThat(goal)
      .hasTitle(createGoalRequest.title)
      .hasNumberOfSteps(createGoalRequest.steps.size)
      .wasCreatedAtPrison(createGoalRequest.prisonId)
      .wasCreatedBy(dpsUsername)
      .hasCreatedByDisplayName(displayName)
      .wasUpdatedBy(dpsUsername)
      .hasUpdatedByDisplayName(displayName)
      .wasUpdatedAtPrison(createGoalRequest.prisonId)
    val step = goal.steps!![0]
    assertThat(step)
      .hasTitle(createStepRequest.title)
      .hasStatus(StepStatus.NOT_STARTED)
      .wasCreatedBy(dpsUsername)

    val notes = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(actionPlan.goals!![0].reference!!, EntityType.GOAL, NoteType.GOAL)
    Assertions.assertThat(notes.size).isGreaterThan(0)
    Assertions.assertThat(notes[0].content).isEqualTo("Chris would like to improve his listening skills, not just his verbal communication")
  }

  @Test
  @Transactional
  fun `should create a new action plan with no review date`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createActionPlanRequest = aValidCreateActionPlanRequest(reviewDate = null)
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
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
      .hasNoReviewDate()
      .wasCreatedBy(dpsUsername)
  }

  @Test
  @Transactional
  fun `should create a new action plan with multiple goals given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createStepRequest1 = aValidCreateStepRequest()
    val createGoalRequest1 = aValidCreateGoalRequest(steps = listOf(createStepRequest1))

    val createStepRequest2 = aValidCreateStepRequest()
    val createGoalRequest2 = aValidCreateGoalRequest(steps = listOf(createStepRequest2), notes = "Goal2 text")

    val createStepRequest3 = aValidCreateStepRequest()
    val createGoalRequest3 = aValidCreateGoalRequest(steps = listOf(createStepRequest3), notes = "Goal3 text")

    val createStepRequest4 = aValidCreateStepRequest()
    val createGoalRequest4 = aValidCreateGoalRequest(steps = listOf(createStepRequest4), notes = "Goal4 text")

    val createActionPlanRequest = aValidCreateActionPlanRequest(goals = listOf(createGoalRequest1, createGoalRequest2, createGoalRequest3, createGoalRequest4))
    val expectedReviewDate = createActionPlanRequest.reviewDate!!
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
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
      .hasReviewDate(expectedReviewDate)
      .hasNumberOfGoals(4)
      .wasCreatedBy(dpsUsername)
    val goal = actionPlan!!.goals!![0]
    assertThat(goal)
      .hasTitle(createGoalRequest1.title)
      .hasNumberOfSteps(createGoalRequest1.steps.size)
      .wasCreatedAtPrison(createGoalRequest1.prisonId)
      .wasCreatedBy(dpsUsername)
      .hasCreatedByDisplayName(displayName)
      .wasUpdatedBy(dpsUsername)
      .hasUpdatedByDisplayName(displayName)
      .wasUpdatedAtPrison(createGoalRequest1.prisonId)
    val step = goal.steps!![0]
    assertThat(step)
      .hasTitle(createStepRequest1.title)
      .hasStatus(StepStatus.NOT_STARTED)
      .wasCreatedBy(dpsUsername)

    val notesForGoal1 = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(actionPlan.goals!![0].reference!!, EntityType.GOAL, NoteType.GOAL)
    Assertions.assertThat(notesForGoal1.size).isGreaterThan(0)
    Assertions.assertThat(notesForGoal1[0].content).isEqualTo("Chris would like to improve his listening skills, not just his verbal communication")

    val notesForGoal2 = noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(actionPlan.goals!![1].reference!!, EntityType.GOAL, NoteType.GOAL)
    Assertions.assertThat(notesForGoal2.size).isGreaterThan(0)
    Assertions.assertThat(notesForGoal2[0].content).isEqualTo("Goal2 text")
  }
}
