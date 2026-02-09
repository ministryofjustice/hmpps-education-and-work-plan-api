package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

class CreateEmployabilitySkillsTest : IntegrationTestBase() {

  companion object {
    const val URI_TEMPLATE = "/action-plans/{prisonNumber}/employability-skills"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, setUpRandomPrisoner())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, setUpRandomPrisoner())
      .withBody(CreateEmployabilitySkillsRequest(listOf(aValidCreateEmployabilitySkillRequest())))
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create action plan given null fields`() {
    val prisonNumber = setUpRandomPrisoner()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
  }

  @Test
  fun `should create an employability skill for a person`() {
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
    val skills = employabilitySkillRepository.findByPrisonNumber(prisonNumber)
    assertThat(skills).hasSize(1)
    assertThat(skills[0].skillType).isEqualTo(EmployabilitySkillType.COMMUNICATION)
    assertThat(skills[0].evidence).isEqualTo("evidence")
    assertThat(skills[0].rating?.code).isEqualTo("VERY_CONFIDENT")
    assertThat(skills[0].rating?.description).isEqualTo("very confident")
    assertThat(skills[0].rating?.score).isEqualTo(4)
    assertThat(skills[0].createdAtPrison).isEqualTo("BXI")
    assertThat(skills[0].prisonNumber).isEqualTo(prisonNumber)
    assertThat(skills[0].updatedAtPrison).isEqualTo("BXI")
    assertThat(skills[0].activityName).isEqualTo("Maths class")
    assertThat(skills[0].conversationDate).isEqualTo(LocalDate.now())

    await.untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient, times(1)).trackEvent(
        eq("EMPLOYABILITY_SKILL_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val createEmployabilitySkillsEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createEmployabilitySkillsEventProperties)
        .containsEntry("prisonId", "BXI")
        .containsEntry("userId", "auser_gen")
        .containsKey("reference")
    }
  }
}
