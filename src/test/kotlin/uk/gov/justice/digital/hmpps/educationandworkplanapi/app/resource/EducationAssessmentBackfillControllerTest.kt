package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.EducationAssessmentEventService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EducationAssessmentBackfillControllerTest {

  @Mock
  private lateinit var educationAssessmentEventService: EducationAssessmentEventService

  @InjectMocks
  private lateinit var controller: EducationAssessmentBackfillController

  @Test
  fun `should call service for each event with correct DTO`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
          detailUrl = "https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC",
        ),
      ),
    )

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    verify(educationAssessmentEventService).process(
      AssessmentEventDto(
        prisonNumber = "A1234BC",
        status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2025, 11, 15),
        detailUrl = "https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC",
      ),
    )
    assertThat(response.totalReceived).isEqualTo(1)
    assertThat(response.totalDeduplicatedInput).isEqualTo(1)
    assertThat(response.totalSaved).isEqualTo(1)
    assertThat(response.failures).isEmpty()
  }

  @Test
  fun `should deduplicate events with same prison number and date`() {
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
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 12, 1),
        ),
      ),
    )

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    verify(educationAssessmentEventService, times(2)).process(any())
    assertThat(response.totalReceived).isEqualTo(3)
    assertThat(response.totalDeduplicatedInput).isEqualTo(2)
    assertThat(response.totalSaved).isEqualTo(2)
    assertThat(response.failures).isEmpty()
  }

  @Test
  fun `should handle empty request`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(events = emptyList())

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    verify(educationAssessmentEventService, never()).process(any())
    assertThat(response.totalReceived).isEqualTo(0)
    assertThat(response.totalDeduplicatedInput).isEqualTo(0)
    assertThat(response.totalSaved).isEqualTo(0)
    assertThat(response.failures).isEmpty()
  }

  @Test
  fun `should catch per-event failures and continue processing`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "B9999ZZ",
          statusChangeDate = LocalDate.of(2025, 11, 20),
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "C5678DE",
          statusChangeDate = LocalDate.of(2025, 11, 25),
        ),
      ),
    )

    // Second event throws (e.g. prisoner not found)
    given(educationAssessmentEventService.process(any()))
      .willAnswer { } // first succeeds
      .willThrow(RuntimeException("Prisoner not found"))
      .willAnswer { } // third succeeds

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    verify(educationAssessmentEventService, times(3)).process(any())
    assertThat(response.totalReceived).isEqualTo(3)
    assertThat(response.totalDeduplicatedInput).isEqualTo(3)
    assertThat(response.totalSaved).isEqualTo(2)
    assertThat(response.failures).hasSize(1)
    assertThat(response.failures[0].prisonNumber).isEqualTo("B9999ZZ")
    assertThat(response.failures[0].error).isEqualTo("Prisoner not found")
  }

  @Test
  fun `should pass null detailUrl through to service`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
      ),
    )

    // When
    controller.backfillAssessmentEvents(request)

    // Then
    verify(educationAssessmentEventService).process(
      AssessmentEventDto(
        prisonNumber = "A1234BC",
        status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2025, 11, 15),
        detailUrl = null,
      ),
    )
  }
}
