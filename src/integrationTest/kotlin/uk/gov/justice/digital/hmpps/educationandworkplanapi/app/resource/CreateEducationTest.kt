package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidCreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.OffsetDateTime

class CreateEducationTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/person/{prisonNumber}/education"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateEducationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create education given no education data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  fun `should create a prisoner's education record given there is no education for the prisoner already`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val earliestCreateTime = OffsetDateTime.now()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidCreateEducationRequest(prisonId = "MDI"))
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated

    // Then
    val education = getEducation(prisonNumber)
    assertThat(education)
      .wasCreatedAtOrAfter(earliestCreateTime)
      .hasNumberOfQualifications(2)
      .qualification(1) {
        it.wasCreatedAtPrison("MDI")
          .wasUpdatedAtPrison("MDI")
      }
      .qualification(2) {
        it.wasCreatedAtPrison("MDI")
          .wasUpdatedAtPrison("MDI")
      }
  }

  @Test
  fun `should fail to create a prisoner's education record given there is an education record for the prisoner already`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    createEducation(prisonNumber)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidCreateEducationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isEqualTo(CONFLICT)
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(409)
      .hasUserMessage("An Education already exists for prisoner A1234BC")
  }
}
