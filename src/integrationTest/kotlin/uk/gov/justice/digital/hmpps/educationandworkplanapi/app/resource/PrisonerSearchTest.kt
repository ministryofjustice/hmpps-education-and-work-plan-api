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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import java.time.LocalDate

class PrisonerSearchTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/search/prisons/{prisonId}/people"
    private const val PRISON_ID = "BXI"
  }

  val prisoner1 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
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
      .hasDeveloperMessage("Access denied on uri=/search/prisons/${PRISON_ID}/people")
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
    assertThat(actual!!.people.size).isZero()
  }

  @Test
  fun `should return 6 people`() {
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
    assertThat(actual!!.people.size).isEqualTo(6)
  }

  @Test
  fun `sort by planLastUpdated asc`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithSort("planLastUpdated")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.people.size).isEqualTo(6)
    assertThat(actual.people[0].planLastUpdated).isNotNull()
  }

  @Test
  fun `filter on prisonerNumber`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithPrisonNameNumberFilter(prisoner1.prisonerNumber)

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.people.size).isEqualTo(1)
    assertThat(actual.people[0].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
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
    val response = searchPeopleWithPrisonNameNumberFilter("bruce")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.people.size).isEqualTo(1)
    assertThat(actual.people[0].prisonNumber).isEqualTo(prisoner6.prisonerNumber)
  }

  @Test
  fun `filter on prisoner name many result`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithPrisonNameNumberFilter("smith")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.people.size).isEqualTo(5)
  }

  @Test
  fun `sort by planLastUpdated desc test`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = searchPeopleWithSort("planLastUpdated", "desc")

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.people.size).isEqualTo(6)
    assertThat(actual.people[0].planLastUpdated).isNull()
  }

  private fun searchPeople(): FluxExchangeResult<PersonSearchResult> {
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(PersonSearchResult::class.java)
    return response
  }

  private fun searchPeopleWithPrisonNameNumberFilter(
    prisonNameNumber: String,
  ): FluxExchangeResult<PersonSearchResult> {
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?prisonerNameOrNumber=$prisonNameNumber", PRISON_ID)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(PersonSearchResult::class.java)
    return response
  }

  private fun searchPeopleWithSort(
    sortBy: String,
    sortDirection: String = "asc",
  ): FluxExchangeResult<PersonSearchResult> {
    val response = webTestClient.get()
      .uri("$URI_TEMPLATE?sortBy=$sortBy&sortDirection=$sortDirection", PRISON_ID)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(PersonSearchResult::class.java)
    return response
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
