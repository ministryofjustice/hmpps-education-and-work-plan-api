package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
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
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should persist backfill events with correct field values`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
          detailUrl = "https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC",
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "D5678EF",
          statusChangeDate = LocalDate.of(2025, 12, 1),
          detailUrl = null,
        ),
      ),
    )

    // When
    val response = webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(EducationAssessmentBackfillController.BackfillResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response.totalReceived).isEqualTo(2)
    assertThat(response.totalDeduplicatedInput).isEqualTo(2)
    assertThat(response.totalSaved).isEqualTo(2)

    val eventsA = educationAssessmentEventRepository.findByPrisonNumber("A1234BC")
    assertThat(eventsA).hasSize(1)
    with(eventsA[0]) {
      assertThat(prisonNumber).isEqualTo("A1234BC")
      assertThat(status).isEqualTo(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
      assertThat(statusChangeDate).isEqualTo(LocalDate.of(2025, 11, 15))
      assertThat(source).isEqualTo("APP_INSIGHTS")
      assertThat(detailUrl).isEqualTo("https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC")
      assertThat(createdAtPrison).isEqualTo("N/A")
      assertThat(updatedAtPrison).isEqualTo("N/A")
      assertThat(reference).isNotNull()
      assertThat(id).isNotNull()
      assertThat(createdAt).isNotNull()
      assertThat(createdBy).isNotNull()
    }

    val eventsD = educationAssessmentEventRepository.findByPrisonNumber("D5678EF")
    assertThat(eventsD).hasSize(1)
    with(eventsD[0]) {
      assertThat(statusChangeDate).isEqualTo(LocalDate.of(2025, 12, 1))
      assertThat(detailUrl).isNull()
      assertThat(source).isEqualTo("APP_INSIGHTS")
    }
  }

  @Test
  fun `should deduplicate events within request`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
      ),
    )

    // When
    val response = webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(EducationAssessmentBackfillController.BackfillResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response.totalReceived).isEqualTo(2)
    assertThat(response.totalDeduplicatedInput).isEqualTo(1)
    assertThat(response.totalSaved).isEqualTo(1)

    val events = educationAssessmentEventRepository.findByPrisonNumber("A1234BC")
    assertThat(events).hasSize(1)
  }

  @Test
  fun `should allow multiple calls creating additional records`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
      ),
    )

    // When - call twice with same data
    webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri(BACKFILL_URI)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated

    // Then - both calls create records (no cross-request dedup)
    val events = educationAssessmentEventRepository.findByPrisonNumber("A1234BC")
    assertThat(events).hasSize(2)
    assertThat(events.map { it.reference }.distinct()).hasSize(2)
  }
}
