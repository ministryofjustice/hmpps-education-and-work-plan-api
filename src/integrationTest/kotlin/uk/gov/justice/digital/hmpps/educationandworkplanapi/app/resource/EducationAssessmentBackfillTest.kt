package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

@Isolated
class EducationAssessmentBackfillTest : IntegrationTestBase() {

  companion object {
    const val BACKFILL_URI = "/education-assessment-events/backfill"
  }

  @BeforeEach
  fun setUp() {
    educationAssessmentEventRepository.deleteAll()
    timelineRepository.deleteAll()
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(BACKFILL_URI)
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with read only role`() {
    webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(
        EducationAssessmentBackfillController.BackfillRequest(
          events = listOf(
            EducationAssessmentBackfillController.BackfillEvent(
              prisonNumber = "A1234BC",
              statusChangeDate = LocalDate.of(2025, 11, 15),
            ),
          ),
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should persist backfill events with correct field values and create timeline events`() {
    // Given
    val prisonNumber = "A1234BC"
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = prisonNumber,
          statusChangeDate = LocalDate.of(2025, 11, 15),
          detailUrl = "https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC",
        ),
      ),
    )

    // When
    val response = webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(EducationAssessmentBackfillController.BackfillResponse::class.java)
      .responseBody.blockFirst()!!

    // Then - verify response
    assertThat(response.totalReceived).isEqualTo(1)
    assertThat(response.totalSaved).isEqualTo(1)
    assertThat(response.failures).isEmpty()

    // Verify record persisted with correct values
    val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
    assertThat(events).hasSize(1)
    with(events[0]) {
      assertThat(this.prisonNumber).isEqualTo(prisonNumber)
      assertThat(status).isEqualTo(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
      assertThat(statusChangeDate).isEqualTo(LocalDate.of(2025, 11, 15))
      assertThat(source).isEqualTo("CURIOUS")
      assertThat(detailUrl).isEqualTo("https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC")
      assertThat(createdAtPrison).isEqualTo("BXI")
      assertThat(updatedAtPrison).isEqualTo("BXI")
      assertThat(reference).isNotNull()
      assertThat(id).isNotNull()
    }

    // Verify timeline event created
    val timeline = getTimeline(prisonNumber)
    assertThat(timeline.events).anyMatch { it.eventType == TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED }

    // Verify telemetry event sent
    val eventPropertiesCaptor = createCaptor<Map<String, String>>()
    verify(telemetryClient).trackEvent(
      eq("EDUCATION_ASSESSMENT_EVENT_CREATED"),
      capture(eventPropertiesCaptor),
      isNull(),
    )
    val eventProperties = eventPropertiesCaptor.value
    assertThat(eventProperties)
      .containsEntry("prisonNumber", prisonNumber)
      .containsEntry("status", "ALL_RELEVANT_ASSESSMENTS_COMPLETE")
      .containsEntry("source", "CURIOUS")
  }

  @Test
  fun `should process multiple prisoners and create timeline events for each`() {
    // Given
    val prisonNumberA = "A1234BC"
    val prisonNumberB = "D5678EF"
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumberA,
      aValidPrisoner(prisonerNumber = prisonNumberA, prisonId = "BXI"),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumberB,
      aValidPrisoner(prisonerNumber = prisonNumberB, prisonId = "MDI"),
    )

    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = prisonNumberA,
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = prisonNumberB,
          statusChangeDate = LocalDate.of(2025, 12, 1),
        ),
      ),
    )

    // When
    val response = webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(EducationAssessmentBackfillController.BackfillResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response.totalSaved).isEqualTo(2)
    assertThat(response.failures).isEmpty()

    // Verify each prisoner has correct prison ID
    val eventsA = educationAssessmentEventRepository.findByPrisonNumber(prisonNumberA)
    assertThat(eventsA).hasSize(1)
    assertThat(eventsA[0].createdAtPrison).isEqualTo("BXI")

    val eventsB = educationAssessmentEventRepository.findByPrisonNumber(prisonNumberB)
    assertThat(eventsB).hasSize(1)
    assertThat(eventsB[0].createdAtPrison).isEqualTo("MDI")

    // Verify timeline events for both
    val timelineA = getTimeline(prisonNumberA)
    assertThat(timelineA.events).anyMatch { it.eventType == TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED }

    val timelineB = getTimeline(prisonNumberB)
    assertThat(timelineB.events).anyMatch { it.eventType == TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED }

    // Verify telemetry for both
    verify(telemetryClient, times(2)).trackEvent(
      eq("EDUCATION_ASSESSMENT_EVENT_CREATED"),
      org.mockito.kotlin.any(),
      isNull(),
    )
  }
}
