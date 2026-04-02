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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO))
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW))
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
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val skills = getEmployabilitySkills(prisonNumber)
    assertThat(skills)
      .hasNumberOfEmployabilitySkills(1)
      .employabilitySkill(1) {
        it.hasSkillType(EmployabilitySkillType.COMMUNICATION)
          .hasEvidence("evidence")
          .hasSkillRating(EmployabilitySkillRating.VERY_CONFIDENT)
          .hasSessionType(EmployabilitySkillSessionType.CIAG_INDUCTION)
          .hasSessionTypeDescription("Maths class")
          .wasCreatedAtPrison("BXI")
          .wasUpdatedAtPrison("BXI")
      }

    await.untilAsserted {
      // Assert telemetry events
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

      // Assert timeline events
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .anyEvent { it.hasEventType(TimelineEventType.EMPLOYABILITY_SKILL_CREATED) }
    }
  }
}
