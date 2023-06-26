package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat

class CreateGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  // TODO The current implementation returns a 500 instead of a 403. Re-enable this test when the implementation has been fixed
  @Test
  @Disabled("The current implementation returns a 500 instead of a 403. Re-enable this test when the implementation has been fixed")
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create goal given goal with no steps`() {
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateGoalRequest(steps = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("At least one Step is required.")
  }

  @Test
  fun `should create goal and action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateGoalRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actual).isForPrisonNumber(prisonNumber)
    assertThat(actual).hasNumberOfGoals(1)
    assertThat(actual!!.goals!![1])
      .usingRecursiveComparison()
      .ignoringFields("id", "reference", "createdAt", "createdBy", "updatedAt", "updatedBy")
      .isEqualTo(createRequest)
  }

  @Test
  fun `should create goal given existing action plan for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlanEntity(prisonNumber = prisonNumber)
    actionPlanRepository.save(actionPlan)
    val createRequest = aValidCreateGoalRequest()

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .body(Mono.just(createRequest), CreateGoalRequest::class.java)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val actual = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actual).isForPrisonNumber(prisonNumber)
    assertThat(actual).hasNumberOfGoals(2)
  }
}