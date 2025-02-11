package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}"
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
      .withBody(aValidCreateInductionRequest())
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create induction given no induction data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  fun `should fail to create induction given induction already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())

    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("An Induction already exists for prisoner $prisonNumber")
  }

  @Test
  fun `should fail to create induction given malformed prison id`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val invalidPrisonId = "does not meet pattern of 3 upper case letters"
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = invalidPrisonId)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("Validation failed for object='createInductionRequest'")
      .hasDeveloperMessage("[Error on field 'prisonId': rejected value [does not meet pattern of 3 upper case letters], must match \"^[A-Z]{3}\$\"]")
  }

  @Test
  fun `should create an induction for a prisoner looking for work`() {
    // Given
    val prisonNumber = anotherValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerLookingToWork()
    val dpsUsername = "auser_gen"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          username = dpsUsername,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(createRequest.prisonId)
      .wasUpdatedAtPrison(createRequest.prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", createRequest.prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should create an induction for a prisoner not looking for work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork()
    val dpsUsername = "auser_gen"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          username = dpsUsername,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(createRequest.prisonId)
      .wasUpdatedAtPrison(createRequest.prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", createRequest.prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should create an induction for a prisoner whose previous work experience is declared as not relevant`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(
      previousWorkExperiences = aValidCreatePreviousWorkExperiencesRequest(
        hasWorkedBefore = HasWorkedBefore.NOT_RELEVANT,
        hasWorkedBeforeNotRelevantReason = "Prisoner is not looking for work so feels previous work experience is not relevant",
        experiences = emptyList(),
      ),
    )

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .previousWorkExperiences {
        it.hasWorkedBefore(HasWorkedBefore.NOT_RELEVANT)
          .hasWorkedBeforeNotRelevantReason("Prisoner is not looking for work so feels previous work experience is not relevant")
      }
  }

  @Test
  fun `should create an induction and not create a review schedule given the prisoner does not have goals created before the induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateInductionRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction).isNotNull
    // assert that there are no Action Plan Reviews (ie. no Review Schedule)
    webTestClient.get()
      .uri(GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNotFound

    assertThat(reviewScheduleHistoryRepository.findAll()).isEmpty()
  }

  @Test
  fun `should create an induction and create the initial review schedule given the prisoner already has goals created before the induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createActionPlan(prisonNumber, aValidCreateActionPlanRequest())

    val createRequest = aValidCreateInductionRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction).isNotNull
    // assert that there is an Action Plan Reviews object, and that it contains no completed reviews, and the latestReviewSchedule has a SCHEDULED status
    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .hasNumberOfCompletedReviews(0)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
      }
    val reviewScheduleReference = actionPlanReviews.latestReviewSchedule.reference

    assertThat(reviewScheduleHistoryRepository.findAllByReference(reviewScheduleReference)).isNotNull
    assertThat(reviewScheduleHistoryRepository.findAll()).size().isEqualTo(1)
  }

  @Test
  fun `should create induction and create initial review schedule given prisoner already has goals created before the induction, is sentenced without a release date and has the indeterminate flag`() {
    // Given
    val prisonNumber = "X9999XX" // Prisoner X9999XX is sentenced, but with no release date, and the has the `indeterminate` flag set
    createActionPlan(prisonNumber, aValidCreateActionPlanRequest())

    val createRequest = aValidCreateInductionRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction).isNotNull
    // assert that there is an Action Plan Reviews object, and that it contains no completed reviews, and the latestReviewSchedule has a SCHEDULED status
    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .hasNumberOfCompletedReviews(0)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
      }
    val reviewScheduleReference = actionPlanReviews.latestReviewSchedule.reference

    assertThat(reviewScheduleHistoryRepository.findAllByReference(reviewScheduleReference)).isNotNull
    assertThat(reviewScheduleHistoryRepository.findAll()).size().isEqualTo(1)
  }

  @Test
  fun `should create induction and not create initial review schedule given prisoner already has goals created before the induction, but is an unsupported sentence type for the release schedule`() {
    // Given
    val prisonNumber = "Z9999ZZ" // Prisoner Z9999ZZ is sentenced, but with no release date, which is an unsupported combination when creating the release schedule
    createActionPlan(prisonNumber, aValidCreateActionPlanRequest())

    val createRequest = aValidCreateInductionRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction).isNotNull
    // assert that there are no Action Plan Reviews (ie. no Review Schedule)
    webTestClient.get()
      .uri(GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isNotFound

    assertThat(reviewScheduleHistoryRepository.findAll()).isEmpty()
  }

  @Test
  fun `should create an induction for a prisoner looking for work and complete the existing induction schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerLookingToWork()
    val dpsUsername = "auser_gen"
    // action plan and induction schedule exist
    createActionPlan(prisonNumber)
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = SCHEDULED,
      createdAtPrison = "BXI",
    )

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          username = dpsUsername,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(createRequest.prisonId)
      .wasUpdatedAtPrison(createRequest.prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", createRequest.prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }

    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    assertThat(inductionSchedule!!.scheduleStatus).isEqualTo(COMPLETED)
  }
}
