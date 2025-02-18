package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.buildAccessToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

class SubjectAccessRequestTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/subject-access-request"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .queryParam("prn", aValidPrisonNumber())
          .build()
      }
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val response = webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .queryParam("prn", aValidPrisonNumber())
          .build()
      }
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/subject-access-request")
  }

  @Test
  fun `should return 204 if no content found`() {
    webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .queryParam("prn", aValidPrisonNumber())
          .build()
      }.bearerToken(
        buildAccessToken(
          roles = listOf("ROLE_SAR_DATA_ACCESS"),
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isNoContent
      .expectBody().isEmpty
  }

  @Test
  fun `should return 209 error if called with crn param`() {
    webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .queryParam("crn", aValidPrisonNumber())
          .build()
      }.bearerToken(
        buildAccessToken(
          roles = listOf("ROLE_SAR_DATA_ACCESS"),
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isEqualTo(209)
      .expectBody().isEmpty
  }

  @Test
  fun `should return 400 error if called without prn or crn param`() {
    val response = webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .build()
      }.bearerToken(
        buildAccessToken(
          roles = listOf("ROLE_SAR_DATA_ACCESS"),
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessage("One of prn or crn must be supplied.")
      .hasDeveloperMessage("One of prn or crn must be supplied.")
  }

  @Test
  fun `should get induction and goals for specific prisoner without date filtering`() {
    // Given
    val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonNumbers.map {
      createInduction(
        prisonNumber = it,
        createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
      )
      createActionPlan(
        prisonNumber = it,
        createActionPlanRequest = aValidCreateActionPlanRequest(
          goals = listOf(aValidCreateGoalRequest(title = "Goal 1")),
        ),
      )
    }

    // When
    val response = webTestClient.sarRequest(aValidPrisonNumber(), null, null)

    // Then
    val responseBody = response
      .expectStatus().isOk
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val content = objectMapper.convertValue(responseBody!!.content, SubjectAccessRequestContent::class.java)
    with(content) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals?.first()?.title).isEqualTo("Goal 1")
    }
  }

  @Test
  fun `should get induction and goals for specific prisoner with from date filtering`() {
    // Given
    val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonNumbers.map {
      createInduction(
        prisonNumber = it,
        createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
      )
      createActionPlan(
        prisonNumber = it,
        createActionPlanRequest = aValidCreateActionPlanRequest(
          goals = listOf(aValidCreateGoalRequest(title = "Goal 1")),
        ),
      )
    }

    // When
    val responseFromLastWeek = webTestClient.sarRequest(aValidPrisonNumber(), LocalDate.now().minusWeeks(1), null)
    val responseFromTomorrow = webTestClient.sarRequest(aValidPrisonNumber(), LocalDate.now().plusDays(1), null)

    // Then
    val responseBody = responseFromLastWeek
      .expectStatus().isOk
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val fromLastWeekContent = objectMapper.convertValue(responseBody!!.content, SubjectAccessRequestContent::class.java)
    with(fromLastWeekContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals?.first()?.title).isEqualTo("Goal 1")
    }

    responseFromTomorrow.expectStatus().isNoContent
  }

  @Test
  fun `should get induction and goals for specific prisoner with to date filtering`() {
    // Given
    val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonNumbers.map {
      createInduction(
        prisonNumber = it,
        createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
      )
      createActionPlan(
        prisonNumber = it,
        createActionPlanRequest = aValidCreateActionPlanRequest(
          goals = listOf(aValidCreateGoalRequest(title = "Goal 1")),
        ),
      )
    }

    // When
    val responseToLastWeek = webTestClient.sarRequest(aValidPrisonNumber(), null, LocalDate.now().minusWeeks(1))
    val responseToTomorrow = webTestClient.sarRequest(aValidPrisonNumber(), null, LocalDate.now().plusDays(1))

    // Then
    responseToLastWeek.expectStatus().isNoContent

    val responseBody = responseToTomorrow
      .expectStatus().isOk
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val toTomorrowContent = objectMapper.convertValue(responseBody!!.content, SubjectAccessRequestContent::class.java)
    with(toTomorrowContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals?.first()?.title).isEqualTo("Goal 1")
    }
  }

  @Test
  fun `should get induction and goals for specific prisoner with from and to date filtering`() {
    // Given
    val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonNumbers.map {
      createInduction(
        prisonNumber = it,
        createInductionRequest = aValidCreateInductionRequestForPrisonerLookingToWork(),
      )
      createActionPlan(
        prisonNumber = it,
        createActionPlanRequest = aValidCreateActionPlanRequest(
          goals = listOf(aValidCreateGoalRequest(title = "Goal 1")),
        ),
      )
    }

    // When
    val responseThisWeek = webTestClient.sarRequest(aValidPrisonNumber(), LocalDate.now(), LocalDate.now().plusWeeks(1))
    val responseLastWeek = webTestClient.sarRequest(aValidPrisonNumber(), LocalDate.now().minusWeeks(1), LocalDate.now())

    // Then
    val responseBody = responseThisWeek
      .expectStatus().isOk
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val thisWeekContent = objectMapper.convertValue(responseBody!!.content, SubjectAccessRequestContent::class.java)
    with(thisWeekContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals?.first()?.title).isEqualTo("Goal 1")
    }

    responseLastWeek.expectStatus().isNoContent
  }

  private fun WebTestClient.sarRequest(prisonNumber: String, fromDate: LocalDate?, toDate: LocalDate?): WebTestClient.ResponseSpec = webTestClient.get()
    .uri { uriBuilder: UriBuilder ->
      uriBuilder
        .path(URI_TEMPLATE)
        .queryParam("prn", prisonNumber)
        .queryParam("fromDate", fromDate)
        .queryParam("toDate", toDate)
        .build()
    }
    .bearerToken(
      buildAccessToken(
        roles = listOf("ROLE_SAR_DATA_ACCESS"),
        privateKey = keyPair.private,
      ),
    )
    .contentType(MediaType.APPLICATION_JSON)
    .exchange()
}
