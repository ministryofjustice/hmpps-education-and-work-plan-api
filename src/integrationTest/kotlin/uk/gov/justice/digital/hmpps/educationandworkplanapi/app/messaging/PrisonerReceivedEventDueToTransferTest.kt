package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

@Isolated
class PrisonerReceivedEventDueToTransferTest : IntegrationTestBase() {

  companion object {
    private const val ORIGINAL_PRISON = "BXI"
    private const val PRISON_TRANSFERRING_TO = "MDI"
    private const val COMPLETE_REVIEW_URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews"
  }

  @Test
  fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // Set the review dates so that we can assert that they've been changed
    updateReviewScheduleReviewDates(
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().plusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(6),
    )

    val expectedEarliestReviewDate = LocalDate.now()
    val expectedLatestReviewDate = LocalDate.now().plusDays(10)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedules = getReviewSchedules(prisonNumber)
    assertThat(reviewSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the re-scheduled
      .hasNumberOfReviewSchedules(3)
      .reviewScheduleAtVersion(2) {
        // Review Schedule version 2 is the exempted schedule due to transfer
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .reviewScheduleAtVersion(3) {
        // Review Schedule version 3 is the re-scheduled review schedule
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasReviewDateFrom(expectedEarliestReviewDate)
          .hasReviewDateTo(expectedLatestReviewDate)
      }

    // test that outbound events are also created (there would have been 3 in total, but we cleared the queue after the first one in the given block above). The 2 new ones are the ones we are really interested in though)
    val reviewScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
    assertThat(reviewScheduleEvents).hasSize(2)
    assertThat(reviewScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  @Test
  fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule and release date in the past`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner(releaseDate = LocalDate.now().minusDays(1))
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // Set the review dates so that we can assert that they've been changed
    updateReviewScheduleReviewDates(
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().plusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(6),
    )

    val expectedEarliestReviewDate = LocalDate.now()
    val expectedLatestReviewDate = LocalDate.now().plusDays(10)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedules = getReviewSchedules(prisonNumber)
    assertThat(reviewSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the re-scheduled
      .hasNumberOfReviewSchedules(3)
      .reviewScheduleAtVersion(2) {
        // Review Schedule version 2 is the exempted schedule due to transfer
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .reviewScheduleAtVersion(3) {
        // Review Schedule version 3 is the re-scheduled review schedule
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasReviewDateFrom(expectedEarliestReviewDate)
          .hasReviewDateTo(expectedLatestReviewDate)
      }

    // test that outbound events are also created (there would have been 3 in total, but we cleared the queue after the first one in the given block above). The 2 new ones are the ones we are really interested in though)
    val reviewScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
    assertThat(reviewScheduleEvents).hasSize(2)
    assertThat(reviewScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  @Test
  fun `should not update Review Schedule and not send outbound message given 'prisoner received' (transfer) event for prisoner that does not have a Review Schedule at all`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

    // test that no outbound events were created
    assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }

  @Test
  fun `prisoner has had their last review prior to release then has their sentence extended and is transferred new review schedule should be created`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner(releaseDate = LocalDate.now().plusDays(100))
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // complete the review Schedule
    // change the release date for the prisoner
    var prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusDays(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    completeReview(prisonNumber)

    // check that the there is no new review schedule and the review is set to prerelease=true

    val reviewSchedulesBefore = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesBefore.size).isEqualTo(1)
    assertThat(reviewSchedulesBefore.first().scheduleStatus).isEqualTo(COMPLETED)

    val reviewsBefore = reviewRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewsBefore.size).isEqualTo(1)
    assertThat(reviewsBefore.first().preRelease).isTrue

    // change the release date for the prisoner
    prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    // When a prisoner is transferred

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then the new review schedule should be created
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedulesAfter = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesAfter.size).isEqualTo(2)
    assertThat(reviewSchedulesAfter.first().scheduleStatus).isEqualTo(COMPLETED)
    assertThat(reviewSchedulesAfter.last().scheduleStatus).isEqualTo(SCHEDULED)
    assertThat(reviewSchedulesAfter.last().latestReviewDate).isEqualTo(LocalDate.now().plusDays(10))
  }

  @Test
  fun `prisoner has had their last review prior to release and is transferred new review schedule should be created if they have 17 days left to serve`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner(releaseDate = LocalDate.now().plusDays(100))
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // complete the review Schedule
    // change the release date for the prisoner
    var prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusDays(17),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    completeReview(prisonNumber)

    // check that there is no new review schedule and the review is set to prerelease=true

    val reviewSchedulesBefore = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesBefore.size).isEqualTo(1)
    assertThat(reviewSchedulesBefore.first().scheduleStatus).isEqualTo(COMPLETED)

    val reviewsBefore = reviewRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewsBefore.size).isEqualTo(1)
    assertThat(reviewsBefore.first().preRelease).isTrue

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    // When a prisoner is transferred

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then the new review schedule should be created
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedulesAfter = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesAfter.size).isEqualTo(2)
    assertThat(reviewSchedulesAfter.first().scheduleStatus).isEqualTo(COMPLETED)
    assertThat(reviewSchedulesAfter.last().scheduleStatus).isEqualTo(SCHEDULED)
    assertThat(reviewSchedulesAfter.last().latestReviewDate).isEqualTo(LocalDate.now().plusDays(10))
  }

  @Test
  fun `prisoner has had their last review prior to release and is transferred NO new review schedule should be created if they have less than 17 days left to serve`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner(releaseDate = LocalDate.now().plusDays(100))
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // complete the review Schedule
    // change the release date for the prisoner
    val prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusDays(16),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    completeReview(prisonNumber)

    // check that there is no new review schedule and the review is set to prerelease=true

    val reviewSchedulesBefore = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesBefore.size).isEqualTo(1)
    assertThat(reviewSchedulesBefore.first().scheduleStatus).isEqualTo(COMPLETED)

    val reviewsBefore = reviewRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewsBefore.size).isEqualTo(1)
    assertThat(reviewsBefore.first().preRelease).isTrue

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    // When a prisoner is transferred

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then the new review schedule should be created
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedulesAfter = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesAfter.size).isEqualTo(1)
    assertThat(reviewSchedulesAfter.last().scheduleStatus).isEqualTo(COMPLETED)
    assertThat(reviewSchedulesAfter.last().latestReviewDate).isEqualTo(LocalDate.now().plusMonths(3))
  }

  @Test
  fun `prisoner has had their last review prior to release then is transferred new review schedule should be created`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner(releaseDate = LocalDate.now().plusDays(100))
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // complete the review Schedule
    // change the release date for the prisoner
    var prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusDays(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    completeReview(prisonNumber)

    // check that the there is no new review schedule and the review is set to prerelease=true

    val reviewSchedulesBefore = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesBefore.size).isEqualTo(1)
    assertThat(reviewSchedulesBefore.first().scheduleStatus).isEqualTo(COMPLETED)

    val reviewsBefore = reviewRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewsBefore.size).isEqualTo(1)
    assertThat(reviewsBefore.first().preRelease).isTrue

    // change the release date for the prisoner to be in the past
    prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().minusDays(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    // When a prisoner is transferred

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then the new review schedule should be created
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val reviewSchedulesAfter = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedulesAfter.size).isEqualTo(2)
    val schedule = reviewSchedulesAfter.findLast { it.scheduleStatus == SCHEDULED }
    assertThat(schedule!!.latestReviewDate).isEqualTo(LocalDate.now().plusDays(10))
  }

  private fun completeReview(prisonNumber: String) {
    webTestClient.post()
      .uri(COMPLETE_REVIEW_URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidCreateActionPlanReviewRequest(
          prisonId = "MDI",
          conductedBy = "Barnie Jones",
          conductedByRole = "Peer mentor",
          note = "A great review today; prisoner is making good progress towards his goals",
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(CreateActionPlanReviewResponse::class.java)
  }
}
