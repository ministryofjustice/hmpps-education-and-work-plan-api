package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonPeriod
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidSignificantMovementAdmission
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidTransferDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aFullyPopulatedCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class GetTimelineTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/timelines/{prisonNumber}"
    private const val CREATE_GOALS_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
    private const val UPDATE_GOAL_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, randomValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

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
    val prisonNumber = randomValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineNotFound(prisonNumber)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          TIMELINE_RO,
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
      .hasUserMessage("Timeline not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should get timeline for prisoner`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val validInductionRequest = aFullyPopulatedCreateInductionRequest()
    createInduction(prisonNumber, validInductionRequest)
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("MDI", "BXI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "MDI"),
            ),
            transfers = listOf(
              aValidTransferDetail(fromPrisonId = "MDI", toPrisonId = "BXI"),
            ),
          ),
        ),
      ),
    )
    // Need this delay as async creation of timelines causes problems
    shortDelay(500)

    val createActionPlanRequest = aValidCreateActionPlanRequest(
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)

    val actionPlan = getActionPlan(prisonNumber)
    val goal = actionPlan.goals[0]
    val induction = getInduction(prisonNumber)

    // When
    await.untilAsserted {
      val response = webTestClient.get()
        .uri(URI_TEMPLATE, prisonNumber)
        .bearerToken(
          aValidTokenWithAuthority(
            TIMELINE_RO,
            privateKey = keyPair.private,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(TimelineResponse::class.java)

      // Then
      val actual = response.responseBody.blockFirst()!!
      val actionPlanCreatedCorrelationId = actual.events[4].correlationId
      assertThat(actual)
        .isForPrisonNumber(prisonNumber)
        .hasNumberOfEvents(6)
        .event(1) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_ADMISSION)
            .hasPrisonId("MDI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasNoContextualInfo()
        }
        .event(2) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_TRANSFER)
            .hasPrisonId("BXI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasContextualInfo(mapOf("PRISON_TRANSFERRED_FROM" to "MDI"))
        }
        .event(3) {
          it.hasEventType(TimelineEventType.INDUCTION_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
            .hasContextualInfo(
              mapOf(
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE" to validInductionRequest.conductedAt.toString(),
                "COMPLETED_INDUCTION_ENTERED_ONLINE_AT" to induction.createdAt.toString(),
                "COMPLETED_INDUCTION_NOTES" to validInductionRequest.note.toString(),
                "COMPLETED_INDUCTION_ENTERED_ONLINE_BY" to "Albert User",
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY" to validInductionRequest.conductedBy.toString(),
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE" to validInductionRequest.conductedByRole.toString(),
              ),
            )
        }
        // Events 4, 5 and 6 were all created as part of the same Action Plan created event so will all have the same timestamp
        // so we cannot guarantee their order
        .anyOfEventNumber(4, 5, 6) {
          it.hasSourceReference(actionPlan.reference.toString())
            .hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .anyOfEventNumber(4, 5, 6) {
          it.hasSourceReference(goal.goalReference.toString())
            .hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
            .hasContextualInfo(mapOf("GOAL_TITLE" to goal.title))
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .anyOfEventNumber(4, 5, 6) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_CREATED)
            .hasPrisonId("BXI")
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
        }
    }
  }

  @Test
  fun `should get timeline with multiple events in order`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    createInduction(prisonNumber, aFullyPopulatedCreateInductionRequest())

    val prisonerFromApi = aValidPrisoner(prisonerNumber = prisonNumber)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("MDI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "MDI"),
            ),
            transfers = emptyList(),
          ),
        ),
      ),
    )

    val validInductionRequest = aFullyPopulatedCreateInductionRequest()

    val createActionPlanRequest = aValidCreateActionPlanRequest(
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)

    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn French"))

    val induction = getInduction(prisonNumber)
    val actionPlan = getActionPlan(prisonNumber)
    val actionPlanReference = actionPlan.reference
    val goal1Reference =
      actionPlan.goals[1].goalReference // The Action Plan returned by the API has Goals in reverse chronological order. The first Goal created is the 2nd in the list
    val goal2Reference = actionPlan.goals[0].goalReference // and the 2nd Goal created is the first in the list.
    val stepToUpdate = actionPlan.goals[1].steps[0]

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goal1Reference,
      title = "Learn Spanish",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepToUpdate.stepReference,
          title = "Research course options",
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )
    updateGoal(prisonNumber, updateGoalRequest)

    // When
    await.untilAsserted {
      val response = webTestClient.get()
        .uri(URI_TEMPLATE, prisonNumber)
        .bearerToken(
          aValidTokenWithAuthority(
            TIMELINE_RO,
            privateKey = keyPair.private,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(TimelineResponse::class.java)

      // Then
      val actual = response.responseBody.blockFirst()!!
      val actionPlanCreatedCorrelationId = actual.events[2].correlationId
      val goalUpdatedCorrelationId = actual.events[6].correlationId
      assertThat(actual)
        .isForPrisonNumber(prisonNumber)
        .hasNumberOfEvents(9)
        .event(1) {
          it.hasSourceReference("1")
            .hasEventType(TimelineEventType.PRISON_ADMISSION)
            .hasPrisonId("MDI")
            .wasActionedBy("system")
            .hasNoActionedByDisplayName()
            .hasNoContextualInfo()
        }
        .event(2) {
          it.hasEventType(TimelineEventType.INDUCTION_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(induction.reference.toString())
            .hasContextualInfo(
              mapOf(
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE" to validInductionRequest.conductedAt.toString(),
                "COMPLETED_INDUCTION_ENTERED_ONLINE_AT" to induction.createdAt.toString(),
                "COMPLETED_INDUCTION_NOTES" to validInductionRequest.note.toString(),
                "COMPLETED_INDUCTION_ENTERED_ONLINE_BY" to "Albert User",
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY" to validInductionRequest.conductedBy.toString(),
                "COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE" to validInductionRequest.conductedByRole.toString(),
              ),
            )
            .correlationIdIsNotEqualTo(actionPlanCreatedCorrelationId)
            .correlationIdIsNotEqualTo(goalUpdatedCorrelationId)
        }
        // Events 3 and 4 were all created as part of the same Action Plan created event so will all have the same timestamp
        // so we cannot guarantee their order
        .anyOfEventNumber(3, 4) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(actionPlanReference.toString())
            .hasNoContextualInfo()
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .anyOfEventNumber(3, 4) {
          it.hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal1Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn German"))
            .hasCorrelationId(actionPlanCreatedCorrelationId)
        }
        .event(5) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_CREATED)
            .hasPrisonId("BXI")
        }
        .event(6) {
          it.hasEventType(TimelineEventType.GOAL_CREATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal2Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn French"))
            .correlationIdIsNotEqualTo(actionPlanCreatedCorrelationId)
            .correlationIdIsNotEqualTo(goalUpdatedCorrelationId)
        }
        // Events 6, 7 and 8 were all created as part of the same Goal Update event so will all have the same timestamp
        // so we cannot guarantee their order
        .anyOfEventNumber(7, 8, 9) {
          it.hasEventType(TimelineEventType.GOAL_UPDATED)
            .hasPrisonId("BXI")
            .hasSourceReference(goal1Reference.toString())
            .hasContextualInfo(mapOf("GOAL_TITLE" to "Learn Spanish")) // Learn German changed to Learn Spanish
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
        .anyOfEventNumber(7, 8, 9) {
          it.hasEventType(TimelineEventType.STEP_STARTED)
            .hasPrisonId("BXI")
            .hasSourceReference(stepToUpdate.stepReference.toString())
            .hasContextualInfo(mapOf("STEP_TITLE" to "Research course options"))
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
        .anyOfEventNumber(7, 8, 9) {
          it.hasEventType(TimelineEventType.STEP_UPDATED)
            .hasPrisonId("BXI")
            .hasSourceReference(stepToUpdate.stepReference.toString())
            .hasContextualInfo(mapOf("STEP_TITLE" to "Research course options"))
            .hasCorrelationId(goalUpdatedCorrelationId)
        }
    }
  }

  @Test
  fun `should get timeline for prisoner with no plp events`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("BXI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "BXI"),
            ),
            transfers = emptyList(),
          ),
        ),
      ),
    )

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          TIMELINE_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(TimelineResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNumberOfEvents(1)
      .event(1) {
        it.hasSourceReference("1")
          .hasEventType(TimelineEventType.PRISON_ADMISSION)
          .hasPrisonId("BXI")
          .wasActionedBy("system")
          .hasNoActionedByDisplayName()
          .hasNoContextualInfo()
      }
  }

  @Nested
  inner class Filtering {
    @Test
    fun `should get review filtered timeline`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=true&inductions=false&goals=false&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(1)
      }
    }

    @Test
    fun `should get induction filtered timeline`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=false&inductions=true&goals=false&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(2)
      }
    }

    @Test
    fun `should get goal filtered timeline`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=false&inductions=false&goals=true&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(6)
      }
    }

    @Test
    fun `should get prisonEvents filtered timeline`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=false&inductions=false&goals=false&prisonEvents=true", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(1)
      }
    }

    @Test
    fun `should get filtered timeline for all event types`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=true&inductions=true&goals=true&prisonEvents=true", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(9)
      }
    }

    @Test
    fun `should get filtered timeline for no event types`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?reviews=false&inductions=false&goals=false&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(9)
      }
    }

    @Test
    fun `should get timeline with no filtering`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri(URI_TEMPLATE, prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(9)
      }
    }

    @Test
    fun `should get filtered timeline filtered on prisonId`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?prisonId=BXI&reviews=false&inductions=false&goals=false&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(8)
      }
    }

    @Test
    fun `should get filtered timeline filtered on prisonId no results`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      setUpAPersonWithLotsOfEvents(prisonNumber)

      // Then
      await.untilAsserted {
        val response = webTestClient.get()
          .uri("${URI_TEMPLATE}?prisonId=XXX&reviews=false&inductions=false&goals=false&prisonEvents=false", prisonNumber)
          .bearerToken(
            aValidTokenWithAuthority(
              TIMELINE_RO,
              privateKey = keyPair.private,
            ),
          )
          .exchange()
          .expectStatus()
          .isOk
          .returnResult(TimelineResponse::class.java)

        val actual = response.responseBody.blockFirst()!!
        assertThat(actual)
          .isForPrisonNumber(prisonNumber)
          .hasNumberOfEvents(0)
      }
    }
  }

  private fun createGoal(prisonNumber: String, createGoalRequest: CreateGoalRequest) {
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  private fun updateGoal(prisonNumber: String, updateGoalRequest: UpdateGoalRequest) {
    webTestClient.put()
      .uri(UPDATE_GOAL_URI_TEMPLATE, prisonNumber, updateGoalRequest.goalReference)
      .withBody(updateGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()
  }

  private fun setUpAPersonWithLotsOfEvents(prisonNumber: String) {
    createInduction(prisonNumber, aFullyPopulatedCreateInductionRequest())

    val prisonerFromApi = aValidPrisoner(prisonerNumber = prisonNumber)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)
    wiremockService.stubGetPrisonTimelineFromPrisonApi(
      prisonNumber,
      aValidPrisonerInPrisonSummary(
        prisonerNumber = prisonNumber,
        prisonPeriod = listOf(
          aValidPrisonPeriod(
            prisons = listOf("MDI"),
            bookingId = 1L,
            movementDates = listOf(
              aValidSignificantMovementAdmission(admittedIntoPrisonId = "MDI"),
            ),
            transfers = emptyList(),
          ),
        ),
      ),
    )

    val createActionPlanRequest = aValidCreateActionPlanRequest(
      goals = listOf(aValidCreateGoalRequest(title = "Learn German")),
    )
    createActionPlan(prisonNumber, createActionPlanRequest)
    createGoal(prisonNumber, aValidCreateGoalRequest(title = "Learn French"))

    val actionPlan = getActionPlan(prisonNumber)
    val goal1Reference =
      actionPlan.goals[1].goalReference // The Action Plan returned by the API has Goals in reverse chronological order. The first Goal created is the 2nd in the list
    val stepToUpdate = actionPlan.goals[1].steps[0]

    val updateGoalRequest = aValidUpdateGoalRequest(
      goalReference = goal1Reference,
      title = "Learn Spanish",
      steps = listOf(
        aValidUpdateStepRequest(
          stepReference = stepToUpdate.stepReference,
          title = "Research course options",
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )
    updateGoal(prisonNumber, updateGoalRequest)

    // When
    await.untilAsserted {
      val response = webTestClient.get()
        .uri(URI_TEMPLATE, prisonNumber)
        .bearerToken(
          aValidTokenWithAuthority(
            TIMELINE_RO,
            privateKey = keyPair.private,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(TimelineResponse::class.java)

      val actual = response.responseBody.blockFirst()!!
      assertThat(actual)
        .isForPrisonNumber(prisonNumber)
        .hasNumberOfEvents(9)
    }
  }
}
