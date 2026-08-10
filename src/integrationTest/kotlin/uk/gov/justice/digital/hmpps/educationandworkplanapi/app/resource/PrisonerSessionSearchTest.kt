package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.FluxExchangeResult
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

class PrisonerSessionSearchTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/session/prisons/{prisonId}/search"
    private const val PRISON_ID = "BXI"
  }

  val prisoner1 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber(), releaseDate = LocalDate.now().plusYears(30), firstName = "John", lastName = "Rambo")
  val prisoner2 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner3 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner4 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner5 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner6 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber(), firstName = "Bruce", lastName = "Wayne")

  // Two screener pending prisoners, deliberately given names, cell locations and release dates that sort
  // in the opposite order to each other, so that each sortable column can be told apart from the others.
  val prisoner7 = aValidPrisoner(
    prisonerNumber = randomValidPrisonNumber(),
    firstName = "Peter",
    lastName = "Zimmerman",
    cellLocation = "A-1-001",
    releaseDate = LocalDate.now().plusYears(5),
  )
  val prisoner8 = aValidPrisoner(
    prisonerNumber = randomValidPrisonNumber(),
    firstName = "Clark",
    lastName = "Adams",
    cellLocation = "C-3-033",
    releaseDate = LocalDate.now().plusYears(2),
  )

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .bearerToken(aValidTokenWithNoAuthorities())
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/session/prisons/${PRISON_ID}/search")
  }

  @Test
  fun `should return zero results`() {
    // Given
    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(),
    )

    // When
    val response = searchPeople()

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isZero()
  }

  @Test
  fun `default search should return all sessions that are DUE`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeople()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionType.INDUCTION)
    assertThat(actual.sessions[1].sessionType).isEqualTo(SessionType.REVIEW)
  }

  @Test
  fun `sort by release date ascending`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithSort(SearchSortField.RELEASE_DATE.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner4.prisonerNumber)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
  }

  @Test
  fun `sort by release date descending`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithSort(SearchSortField.RELEASE_DATE.name, SearchSortDirection.DESC.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner4.prisonerNumber)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
  }

  @Test
  fun `filter on prisonerNumber`() {
    // Given
    setUpData()

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisoner1.prisonerNumber,
      prisoner1,
    )

    // When
    val response = searchPeopleWithPrisonNameNumberFilter(prisoner1.prisonerNumber)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
  }

  @Test
  fun `filter on prisoner name one result`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithPrisonNameNumberFilter("rambo")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
  }

  @Test
  fun `filter on prisoner name, sessionType and sessionStatusType many result`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithSessionStatusTypeAndName("smith", "OVERDUE")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
  }

  @Test
  fun `filter on overdue sessions`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithActionSessionStatusType(SessionStatusType.OVERDUE.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner2.prisonerNumber)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner5.prisonerNumber)
  }

  @Test
  fun `filter by REVIEW should bring back all due review sessions`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithActionSessionType(SessionType.REVIEW.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull

    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner4.prisonerNumber)
  }

  @Test
  fun `should return prisoner with TRANSFER_REVIEW reviewType`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber)
    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner),
    )

    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = "BXI"))
    createActionPlan(prisonNumber)

    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = "WWW",
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

    // When
    // When
    val response = searchPeople()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionType.TRANSFER_REVIEW)
  }

  @Test
  fun `filter on screener pending sessions`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val response = searchPeopleWithActionSessionStatusType(SessionStatusType.SCREENER_PENDING.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    // default sort is by prisoner name ascending, so Adams comes before Zimmerman
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner8.prisonerNumber)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner7.prisonerNumber)
    // screener pending only ever applies to Inductions, and such a session is not an exemption
    assertThat(actual.sessions).allSatisfy {
      assertThat(it.sessionType).isEqualTo(SessionType.INDUCTION)
      assertThat(it.exemptionReason).isNull()
      assertThat(it.exemptionDate).isNull()
    }
  }

  @Test
  fun `screener pending sessions do not appear in the on hold, due or overdue lists`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val onHold = searchPeopleWithActionSessionStatusType(SessionStatusType.ON_HOLD.name).responseBody.blockFirst()
    val due = searchPeopleWithActionSessionStatusType(SessionStatusType.DUE.name).responseBody.blockFirst()
    val overdue = searchPeopleWithActionSessionStatusType(SessionStatusType.OVERDUE.name).responseBody.blockFirst()

    // Then
    val screenerPendingPrisonNumbers = setOf(prisoner7.prisonerNumber, prisoner8.prisonerNumber)
    assertThat(onHold!!.sessions.map { it.prisonNumber }).doesNotContainAnyElementsOf(screenerPendingPrisonNumbers)
    assertThat(due!!.sessions.map { it.prisonNumber }).doesNotContainAnyElementsOf(screenerPendingPrisonNumbers)
    assertThat(overdue!!.sessions.map { it.prisonNumber }).doesNotContainAnyElementsOf(screenerPendingPrisonNumbers)
  }

  @Test
  fun `sort screener pending sessions by prisoner name`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val ascending = searchScreenerPendingWithSort(SessionSearchSortField.PRISONER_NAME.name, SearchSortDirection.ASC.name)
    val descending = searchScreenerPendingWithSort(SessionSearchSortField.PRISONER_NAME.name, SearchSortDirection.DESC.name)

    // Then
    assertThat(ascending.map { it.prisonNumber }).containsExactly(prisoner8.prisonerNumber, prisoner7.prisonerNumber)
    assertThat(descending.map { it.prisonNumber }).containsExactly(prisoner7.prisonerNumber, prisoner8.prisonerNumber)
  }

  @Test
  fun `sort screener pending sessions by cell location`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val ascending = searchScreenerPendingWithSort(SessionSearchSortField.CELL_LOCATION.name, SearchSortDirection.ASC.name)
    val descending = searchScreenerPendingWithSort(SessionSearchSortField.CELL_LOCATION.name, SearchSortDirection.DESC.name)

    // Then
    // prisoner7 is in A-1-001, prisoner8 is in C-3-033
    assertThat(ascending.map { it.prisonNumber }).containsExactly(prisoner7.prisonerNumber, prisoner8.prisonerNumber)
    assertThat(descending.map { it.prisonNumber }).containsExactly(prisoner8.prisonerNumber, prisoner7.prisonerNumber)
  }

  @Test
  fun `sort screener pending sessions by release date`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val ascending = searchScreenerPendingWithSort(SessionSearchSortField.RELEASE_DATE.name, SearchSortDirection.ASC.name)
    val descending = searchScreenerPendingWithSort(SessionSearchSortField.RELEASE_DATE.name, SearchSortDirection.DESC.name)

    // Then
    // prisoner8 is released in 2 years, prisoner7 in 5 years
    assertThat(ascending.map { it.prisonNumber }).containsExactly(prisoner8.prisonerNumber, prisoner7.prisonerNumber)
    assertThat(descending.map { it.prisonNumber }).containsExactly(prisoner7.prisonerNumber, prisoner8.prisonerNumber)
  }

  @Test
  fun `filter screener pending sessions on prisoner name`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val response = searchPeopleWithSessionStatusTypeAndName("Zimmerman", SessionStatusType.SCREENER_PENDING.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner7.prisonerNumber)
  }

  @Test
  fun `filter screener pending sessions on prison number`() {
    // Given
    setUpData()

    stubAllPrisoners()
    // searching by a valid prison number looks the prisoner up individually rather than filtering the
    // prison roster, so that lookup needs stubbing too - see AbstractPrisonerSearchService.getPrisonerList
    createPrisonerAPIStub(prisoner8.prisonerNumber, prisoner8)

    // When
    val response =
      searchPeopleWithSessionStatusTypeAndName(prisoner8.prisonerNumber, SessionStatusType.SCREENER_PENDING.name)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner8.prisonerNumber)
  }

  @Test
  fun `screener pending sessions are paginated`() {
    // Given
    setUpData()

    stubAllPrisoners()

    // When
    val response = searchPeopleWithParams(
      "sessionStatusType" to SessionStatusType.SCREENER_PENDING.name,
      "pageSize" to "1",
      "page" to "2",
    )

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    // page 2 of the default (name ascending) sort is Zimmerman
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner7.prisonerNumber)
    assertThat(actual.pagination!!.totalElements).isEqualTo(2)
    assertThat(actual.pagination!!.totalPages).isEqualTo(2)
    assertThat(actual.pagination!!.page).isEqualTo(2)
  }

  private fun stubAllPrisoners() = wiremockService.stubPrisonersInAPrisonSearchApi(
    PRISON_ID,
    listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6, prisoner7, prisoner8),
  )

  private fun searchScreenerPendingWithSort(sortBy: String, sortDirection: String) = searchPeopleWithParams(
    "sessionStatusType" to SessionStatusType.SCREENER_PENDING.name,
    "sortBy" to sortBy,
    "sortDirection" to sortDirection,
  ).responseBody.blockFirst()!!.sessions

  private fun searchPeople(): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams()

  private fun searchPeopleWithPrisonNameNumberFilter(prisonNameNumber: String): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams("prisonerNameOrNumber" to prisonNameNumber)

  private fun searchPeopleWithActionSessionStatusType(sessionStatusType: String): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams("sessionStatusType" to sessionStatusType)

  private fun searchPeopleWithActionSessionType(sessionType: String): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams("sessionType" to sessionType)

  private fun searchPeopleWithSessionStatusTypeAndName(
    prisonerNameOrNumber: String,
    sessionStatusType: String,
  ): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams(
    "prisonerNameOrNumber" to prisonerNameOrNumber,
    "sessionStatusType" to sessionStatusType,
  )

  private fun searchPeopleWithSort(sortBy: String, sortDirection: String = SearchSortDirection.ASC.name): FluxExchangeResult<SessionSearchResponses> = searchPeopleWithParams("sortBy" to sortBy, "sortDirection" to sortDirection)

  private fun searchPeopleWithParams(vararg params: Pair<String, String>): FluxExchangeResult<SessionSearchResponses> {
    val uri = URI_TEMPLATE + params.joinToString("&", prefix = "?") { "${it.first}=${it.second}" }

    return webTestClient.get()
      .uri(uri, PRISON_ID)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, ACTIONPLANS_RW))
      .exchange()
      .expectStatus().isOk
      .returnResult(SessionSearchResponses::class.java)
  }

  fun setUpData() {
    createActionPlan(prisoner1.prisonerNumber)
    // due induction
    createInductionSchedule(prisoner1.prisonerNumber, deadlineDate = LocalDate.now().plusDays(1), status = SCHEDULED)
    // overdue induction
    createInductionSchedule(prisoner2.prisonerNumber, deadlineDate = LocalDate.now().minusDays(1), status = SCHEDULED)
    // exempt induction
    createInductionSchedule(
      prisoner3.prisonerNumber,
      deadlineDate = LocalDate.now().plusDays(1),
      status = EXEMPT_PRISONER_SAFETY_ISSUES,
    )
    // due review
    createReviewScheduleRecord(
      prisoner4.prisonerNumber,
      latestDate = LocalDate.now().plusDays(1),
      earliestDate = LocalDate.now().minusDays(10),
      status = ReviewScheduleStatus.SCHEDULED,
    )
    // overdue review
    createReviewScheduleRecord(
      prisoner5.prisonerNumber,
      latestDate = LocalDate.now().minusDays(1),
      earliestDate = LocalDate.now().minusDays(10),
      status = ReviewScheduleStatus.SCHEDULED,
    )
    // exempt review
    createReviewScheduleRecord(
      prisoner6.prisonerNumber,
      latestDate = LocalDate.now().plusDays(1),
      earliestDate = LocalDate.now().minusDays(10),
      status = ReviewScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
    )
    // screener pending inductions
    createInductionSchedule(
      prisoner7.prisonerNumber,
      deadlineDate = LocalDate.now().plusDays(1),
      status = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    )
    createInductionSchedule(
      prisoner8.prisonerNumber,
      deadlineDate = LocalDate.now().plusDays(1),
      status = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    )
  }
}
