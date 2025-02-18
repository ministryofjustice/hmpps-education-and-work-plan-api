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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import java.time.LocalDate

class GetSessionSummaryTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/session/{prisonId}/summary"
    private const val PRISON_ID = "BXI"
  }

  val prisoner1 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner2 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner3 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner4 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner5 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner6 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())

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
      .hasDeveloperMessage("Access denied on uri=/session/${PRISON_ID}/summary")
  }

  @Test
  fun `should return zero counts`() {
    // Given
    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(aValidPrisoner(prisonerNumber = randomValidPrisonNumber())),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isZero()
    assertThat(actual.dueInductions).isZero()
    assertThat(actual.overdueReviews).isZero()
    assertThat(actual.overdueInductions).isZero()
    assertThat(actual.exemptReviews).isZero()
    assertThat(actual.exemptInductions).isZero()
  }

  @Test
  fun `should return 1 count in each section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5, prisoner6),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(1)
    assertThat(actual.dueInductions).isEqualTo(1)
    assertThat(actual.overdueReviews).isEqualTo(1)
    assertThat(actual.overdueInductions).isEqualTo(1)
    assertThat(actual.exemptReviews).isEqualTo(1)
    assertThat(actual.exemptInductions).isEqualTo(1)
  }

  @Test
  fun `should return 1 count in due induction section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner1),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(0)
    assertThat(actual.dueInductions).isEqualTo(1)
    assertThat(actual.overdueReviews).isEqualTo(0)
    assertThat(actual.overdueInductions).isEqualTo(0)
    assertThat(actual.exemptReviews).isEqualTo(0)
    assertThat(actual.exemptInductions).isEqualTo(0)
  }

  @Test
  fun `should return 1 count in over due induction section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner2),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(0)
    assertThat(actual.dueInductions).isEqualTo(0)
    assertThat(actual.overdueReviews).isEqualTo(0)
    assertThat(actual.overdueInductions).isEqualTo(1)
    assertThat(actual.exemptReviews).isEqualTo(0)
    assertThat(actual.exemptInductions).isEqualTo(0)
  }

  @Test
  fun `should return 1 count in due review section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner4),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(1)
    assertThat(actual.dueInductions).isEqualTo(0)
    assertThat(actual.overdueReviews).isEqualTo(0)
    assertThat(actual.overdueInductions).isEqualTo(0)
    assertThat(actual.exemptReviews).isEqualTo(0)
    assertThat(actual.exemptInductions).isEqualTo(0)
  }

  @Test
  fun `should return 1 count in over due review section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner5),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(0)
    assertThat(actual.dueInductions).isEqualTo(0)
    assertThat(actual.overdueReviews).isEqualTo(1)
    assertThat(actual.overdueInductions).isEqualTo(0)
    assertThat(actual.exemptReviews).isEqualTo(0)
    assertThat(actual.exemptInductions).isEqualTo(0)
  }

  @Test
  fun `should return 1 count in exempt review section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner6),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(0)
    assertThat(actual.dueInductions).isEqualTo(0)
    assertThat(actual.overdueReviews).isEqualTo(0)
    assertThat(actual.overdueInductions).isEqualTo(0)
    assertThat(actual.exemptReviews).isEqualTo(1)
    assertThat(actual.exemptInductions).isEqualTo(0)
  }

  @Test
  fun `should return 1 count in exempt induction section`() {
    // Given
    setUpData()

    wiremockService.stubPrisonersInAPrisonSearchApi(
      PRISON_ID,
      listOf(prisoner3),
    )

    // When
    val response = getSessionSummary()

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.dueReviews).isEqualTo(0)
    assertThat(actual.dueInductions).isEqualTo(0)
    assertThat(actual.overdueReviews).isEqualTo(0)
    assertThat(actual.overdueInductions).isEqualTo(0)
    assertThat(actual.exemptReviews).isEqualTo(0)
    assertThat(actual.exemptInductions).isEqualTo(1)
  }

  private fun getSessionSummary(): FluxExchangeResult<SessionSummaryResponse> {
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, PRISON_ID)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(SessionSummaryResponse::class.java)
    return response
  }

  fun setUpData() {
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
