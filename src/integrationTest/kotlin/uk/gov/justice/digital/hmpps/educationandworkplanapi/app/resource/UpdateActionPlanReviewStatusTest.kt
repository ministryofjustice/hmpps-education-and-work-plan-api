package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidUpdateReviewScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class UpdateActionPlanReviewStatusTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/schedule-status"
  }

  private val prisonNumber = aValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .contentType(APPLICATION_JSON)
      .withBody(aValidCreateActionPlanReviewRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    // Given

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .contentType(APPLICATION_JSON)
      .withBody(aValidUpdateReviewScheduleStatusRequest())
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/action-plans/$prisonNumber/reviews/schedule-status")
  }

  @Test
  fun `should fail to update review schedule status given no review data provided`() {
    // Given

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
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
  fun `should fail to update review status given review schedule does not exist`() {
    // Given
    wiremockService.stubGetPrisonerNotFound(prisonNumber)

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidUpdateReviewScheduleStatusRequest())
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("Review Schedule not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should update review schedule to exempt status`() {
    // Given
    createReviewScheduleRecord(prisonNumber)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY.name)
  }

  @Test
  fun `should update review schedule from exempt back to scheduled status`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY.name,
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
  }

  @Test
  fun `should fail to update review schedule from exempt to exempt status`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY.name,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .is4xxClientError
      .returnResult(ErrorResponse::class.java)

    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(CONFLICT.value())
      .hasUserMessage("Invalid Review Schedule status transition for prisoner [$prisonNumber] status from EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY to EXEMPT_PRISONER_FAILED_TO_ENGAGE")
  }

  @Test
  fun `should fail to update review schedule from exempt to COMPLETED`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY.name,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.COMPLETED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .is4xxClientError
      .returnResult(ErrorResponse::class.java)

    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(CONFLICT.value())
      .hasUserMessage("Invalid Review Schedule status transition for prisoner [$prisonNumber] status from EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY to COMPLETED")
  }

  @Test
  fun `should update review schedule from Scheduled to technical issue`() {
    // Given
    createReviewScheduleRecord(prisonNumber, status = ReviewScheduleStatus.SCHEDULED.name)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
  }

  @Test
  fun `when technical issue should add 5 days to latest review date `() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.SCHEDULED.name,
      earliestDate = LocalDate.now().minusDays(5),
      latestDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(LocalDate.now().plusDays(5))
  }

  @Test
  fun `when exception removed should add 5 days to latest review date `() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE.name,
      earliestDate = LocalDate.now().minusDays(5),
      latestDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(LocalDate.now().plusDays(5))
  }

  @Test
  fun `when exclusion removed should add 10 days to latest review date `() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES.name,
      earliestDate = LocalDate.now().minusDays(5),
      latestDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(LocalDate.now().plusDays(10))
  }

  @Test
  fun `Test follow on events occur`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES.name,
      earliestDate = LocalDate.now().minusDays(5),
      latestDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(LocalDate.now().plusDays(10))

    val today = LocalDate.now().toString()
    val todayPlusTen = LocalDate.now().plusDays(10).toString()
    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(1) { // the 3rd Timeline event will be the GOAL_UPDATED event
          it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED)
            .hasContextualInfo(
              mapOf(
                "REVIEW_SCHEDULE_DEADLINE_NEW" to todayPlusTen,
                "REVIEW_SCHEDULE_DEADLINE_OLD" to today,
                "REVIEW_SCHEDULE_STATUS_OLD" to "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
                "REVIEW_SCHEDULE_STATUS_NEW" to "SCHEDULED",
              ),
            )
        }
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient).trackEvent(
        eq("REVIEW_SCHEDULE_STATUS_UPDATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
    }

    // test that outbound event is also created:
    val reviewScheduleEvents = reviewScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
    assertThat(reviewScheduleEvents[0].personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(reviewScheduleEvents[0].detailUrl)
      .isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
  }
}
