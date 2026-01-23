package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.FluxExchangeResult
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
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
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
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
  }
}
