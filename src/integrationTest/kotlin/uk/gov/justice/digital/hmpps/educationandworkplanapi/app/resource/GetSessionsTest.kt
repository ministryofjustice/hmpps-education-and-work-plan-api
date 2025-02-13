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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerIdsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class GetSessionsTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/session/summary"
  }

  val prisoner1 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner2 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner3 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner4 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner5 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())
  val prisoner6 = aValidPrisoner(prisonerNumber = randomValidPrisonNumber())

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri("$URI_TEMPLATE?status=DUE")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val prisonerIds = listOf(prisoner5.prisonerNumber)
    val prisonerIdsRequest = PrisonerIdsRequest(prisonerIds)
    val response = webTestClient.post()
      .uri("$URI_TEMPLATE?status=DUE")
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
      .withBody(prisonerIdsRequest)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/session/summary")
  }

  @Test
  fun `should return 1 result in overdue sessions`() {
    // Given
    setUpData()

    val prisonerIds = listOf(prisoner5.prisonerNumber)

    // When
    val response = getSessionSummary(status = SessionStatusType.OVERDUE, prisonerIdsRequest = PrisonerIdsRequest(prisonerIds))

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(1)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner5.prisonerNumber)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionResponse.SessionType.REVIEW)
    assertThat(actual.sessions[0].exemptionReason).isNull()
    assertThat(actual.sessions[0].exemptionDate).isNull()
  }

  @Test
  fun `should return 2 results in overdue sessions`() {
    // Given
    setUpData()

    val prisonerIds = listOf(prisoner5.prisonerNumber, prisoner2.prisonerNumber)

    // When
    val response = getSessionSummary(status = SessionStatusType.OVERDUE, prisonerIdsRequest = PrisonerIdsRequest(prisonerIds))

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner5.prisonerNumber)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionResponse.SessionType.REVIEW)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner2.prisonerNumber)
    assertThat(actual.sessions[1].sessionType).isEqualTo(SessionResponse.SessionType.INDUCTION)
  }

  @Test
  fun `should return 2 results in due sessions`() {
    // Given
    setUpData()

    val prisonerIds = listOf(prisoner4.prisonerNumber, prisoner1.prisonerNumber)

    // When
    val response = getSessionSummary(status = SessionStatusType.DUE, prisonerIdsRequest = PrisonerIdsRequest(prisonerIds))

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner4.prisonerNumber)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionResponse.SessionType.REVIEW)
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner1.prisonerNumber)
    assertThat(actual.sessions[1].sessionType).isEqualTo(SessionResponse.SessionType.INDUCTION)
  }

  @Test
  fun `should return 2 results in on hold sessions`() {
    // Given
    setUpData()

    val prisonerIds = listOf(prisoner6.prisonerNumber, prisoner3.prisonerNumber)

    // When
    val response = getSessionSummary(status = SessionStatusType.ON_HOLD, prisonerIdsRequest = PrisonerIdsRequest(prisonerIds))

    // Then
    val actual = response.responseBody.blockFirst()

    assertThat(actual).isNotNull
    assertThat(actual!!.sessions.size).isEqualTo(2)
    assertThat(actual.sessions[0].prisonNumber).isEqualTo(prisoner6.prisonerNumber)
    assertThat(actual.sessions[0].sessionType).isEqualTo(SessionResponse.SessionType.REVIEW)
    assertThat(actual.sessions[0].exemptionReason).isEqualTo("This guy messes around")
    assertThat(actual.sessions[1].prisonNumber).isEqualTo(prisoner3.prisonerNumber)
    assertThat(actual.sessions[1].sessionType).isEqualTo(SessionResponse.SessionType.INDUCTION)
    assertThat(actual.sessions[1].exemptionReason).isEqualTo("Has a drugs problem")
  }

  private fun getSessionSummary(status: SessionStatusType, prisonerIdsRequest: PrisonerIdsRequest): FluxExchangeResult<SessionResponses> {
    val response = webTestClient.post()
      .uri(URI_TEMPLATE + "?status=${status.name}")
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          privateKey = keyPair.private,
        ),
      )
      .withBody(prisonerIdsRequest)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(SessionResponses::class.java)
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
      status = EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
      exemptionReason = "Has a drugs problem",
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
      exemptionReason = "This guy messes around",
    )
  }
}
