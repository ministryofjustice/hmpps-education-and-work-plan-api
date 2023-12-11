package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class UpdateGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .withBody(aValidUpdateGoalRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to update goal given no steps provided`() {
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
      steps = emptyList(),
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
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
      .hasUserMessage("Validation failed for object='updateGoalRequest'. Error count: 1")
      .hasDeveloperMessageContaining("Steps cannot be empty when updating a Goal")
  }

  @Test
  fun `should fail to update goal given null fields`() {
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
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
      .hasUserMessageContaining("value failed for JSON property goalReference due to missing (therefore NULL) value for creator parameter goalReference")
  }

  @Test
  fun `should fail to update goal given goal does not exist`() {
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
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
  fun `should fail to update goal given goal references in URI path and request body do not match`() {
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()
    val someOtherGoalReference = aValidReference()
    val updateRequest = aValidUpdateGoalRequest(
      goalReference = someOtherGoalReference,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateRequest)
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
      .hasUserMessage("Goal reference in URI path must match the Goal reference in the request body")
  }

  @Test
  @Transactional
  fun `should update goal`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createGoalRequest = aValidCreateGoalRequest(
      title = "Learn French",
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

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    val goalReference = actionPlan!!.goals!![0].reference!!
    val stepReference = actionPlan.goals!![0].steps!![0].reference!!

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goalReference,
      title = "Learn French to GCSE standard",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepReference,
          title = "Book course before December 2023",
          sequenceNumber = 1,
        ),
        aValidUpdateStepRequest(
          stepReference = null,
          title = "Attend course before March 2024",
          sequenceNumber = 2,
        ),
      ),
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      prisonId = "MDI",
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = "buser_gen",
          displayName = "Bernie User",
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()

    TestTransaction.end()
    TestTransaction.start()

    // Then
    val actual = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfGoals(1)
      .goal(0) { goal ->
        goal
          .hasTitle("Learn French to GCSE standard")
          .hasNumberOfSteps(2)
          .stepWithSequenceNumber(1) { step ->
            step.hasTitle("Book course before December 2023")
          }
          .stepWithSequenceNumber(2) { step ->
            step.hasTitle("Attend course before March 2024")
          }
          .wasCreatedAtPrison("BXI")
          .wasUpdatedAtPrison("MDI")
          .wasCreatedBy("auser_gen")
          .wasUpdatedBy("buser_gen")
      }

    val timeline = getTimeline(prisonNumber)
    assertThat(timeline)
      .event(3) { // the 3rd Timeline event will be the GOAL_UPDATED event
        it.hasEventType(TimelineEventType.GOAL_UPDATED)
          .wasActionedBy("buser_gen")
          .hasActionedByDisplayName("Bernie User")
      }

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("goal-updated"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      verify(telemetryClient).trackEvent(
        eq("step-removed"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val goalUpdatedEventProperties = eventPropertiesCaptor.firstValue
      val stepRemovedEventProperties = eventPropertiesCaptor.secondValue
      assertThat(goalUpdatedEventProperties)
        .containsEntry("reference", goalReference.toString())
        .containsEntry("notesCharacterCount", "83")
      assertThat(stepRemovedEventProperties)
        .containsEntry("reference", goalReference.toString())
        .containsEntry("stepCount", "2")
      assertThat(goalUpdatedEventProperties["correlationId"])
        .isEqualTo(stepRemovedEventProperties["correlationId"])
    }
  }
}
