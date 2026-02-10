package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType as APIEmployabilitySkillType

class GetEmployabilitySkillsTest : IntegrationTestBase() {

  companion object {
    const val URI_TEMPLATE = "/action-plans/{prisonNumber}/employability-skills"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, setUpRandomPrisoner())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with wrong role`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, setUpRandomPrisoner())
      .bearerToken(aValidTokenWithAuthority(GOALS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should get employability skills for a person`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val createEmployabilitySkillsRequest = CreateEmployabilitySkillsRequest(listOf(aValidCreateEmployabilitySkillRequest()))

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createEmployabilitySkillsRequest)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult<GetEmployabilitySkillResponses>()

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual!!.employabilitySkills).hasSize(1)
    assertThat(actual.employabilitySkills[0].employabilitySkillType).isEqualTo(APIEmployabilitySkillType.COMMUNICATION)
    assertThat(actual.employabilitySkills[0].evidence).isEqualTo("evidence")
    assertThat(actual.employabilitySkills[0].employabilitySkillRating.name).isEqualTo("VERY_CONFIDENT")
    assertThat(actual.employabilitySkills[0].createdAtPrison).isEqualTo("BXI")
    assertThat(actual.employabilitySkills[0].updatedAtPrison).isEqualTo("BXI")
    assertThat(actual.employabilitySkills[0].activityName).isEqualTo("Maths class")
    assertThat(actual.employabilitySkills[0].conversationDate).isEqualTo(LocalDate.now())
  }
}
