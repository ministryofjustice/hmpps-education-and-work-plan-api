package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.SubjectAccessRequestService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.buildAccessToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
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
  fun `should get induction and goals for specific prisoner without date filtering`() {
    // Given
    val prisonerNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonerNumbers.map {
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
      .expectBody(PLPSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val content = responseBody!!.content
    with(content) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals.first().title).isEqualTo("Goal 1")
    }
  }

  @Test
  fun `should get induction and goals for specific prisoner with from date filtering`() {
    // Given
    val prisonerNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonerNumbers.map {
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
      .expectBody(PLPSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val fromLastWeekContent = responseBody!!.content

    with(fromLastWeekContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals.first().title).isEqualTo("Goal 1")
    }

    responseFromTomorrow.expectStatus().isNoContent
  }

  @Test
  fun `should get induction and goals for specific prisoner with to date filtering`() {
    // Given
    val prisonerNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonerNumbers.map {
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
      .expectBody(PLPSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val toTomorrowContent = responseBody!!.content
    with(toTomorrowContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals.first().title).isEqualTo("Goal 1")
    }
  }

  @Test
  fun `should get induction and goals for specific prisoner with from and to date filtering`() {
    // Given
    val prisonerNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())
    prisonerNumbers.map {
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
      .expectBody(PLPSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val thisWeekContent = responseBody!!.content
    with(thisWeekContent) {
      assertThat(induction?.prisonNumber).isEqualTo(aValidPrisonNumber())
      assertThat(goals).hasSize(1)
      assertThat(goals.first().title).isEqualTo("Goal 1")
    }

    responseLastWeek.expectStatus().isNoContent
  }

  private data class PLPSubjectAccessRequestContent(
    val content: SubjectAccessRequestService.SubjectAccessRequestContent,
  )

  private fun WebTestClient.sarRequest(prn: String, fromDate: LocalDate?, toDate: LocalDate?): WebTestClient.ResponseSpec {
    return webTestClient.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(URI_TEMPLATE)
          .queryParam("prn", prn)
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
}
