package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EducationAssessmentBackfillControllerTest {

  @Mock
  private lateinit var educationAssessmentEventRepository: EducationAssessmentEventRepository

  @InjectMocks
  private lateinit var controller: EducationAssessmentBackfillController

  @Test
  fun `should save all events with correct field values`() {
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

    val entitiesCaptor = argumentCaptor<List<EducationAssessmentEventEntity>>()
    given(educationAssessmentEventRepository.saveAll(entitiesCaptor.capture())).willReturn(emptyList())

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    assertThat(response.totalReceived).isEqualTo(1)
    assertThat(response.totalDeduplicatedInput).isEqualTo(1)
    assertThat(response.totalSaved).isEqualTo(1)

    val savedEntities = entitiesCaptor.firstValue
    assertThat(savedEntities).hasSize(1)
    with(savedEntities[0]) {
      assertThat(prisonNumber).isEqualTo("A1234BC")
      assertThat(status).isEqualTo(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
      assertThat(statusChangeDate).isEqualTo(LocalDate.of(2025, 11, 15))
      assertThat(source).isEqualTo("APP_INSIGHTS")
      assertThat(detailUrl).isEqualTo("https://liveservices.sequation.net/sequation-virtual-campus2-api/learnerAssessments/v2//A1234BC")
      assertThat(createdAtPrison).isEqualTo("N/A")
      assertThat(updatedAtPrison).isEqualTo("N/A")
      assertThat(reference).isNotNull()
    }
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

    val entitiesCaptor = argumentCaptor<List<EducationAssessmentEventEntity>>()
    given(educationAssessmentEventRepository.saveAll(entitiesCaptor.capture())).willReturn(emptyList())

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    assertThat(response.totalReceived).isEqualTo(3)
    assertThat(response.totalDeduplicatedInput).isEqualTo(2)
    assertThat(response.totalSaved).isEqualTo(2)

    val savedEntities = entitiesCaptor.firstValue
    assertThat(savedEntities).hasSize(2)
    assertThat(savedEntities.map { it.prisonNumber to it.statusChangeDate }).containsExactly(
      "A1234BC" to LocalDate.of(2025, 11, 15),
      "A1234BC" to LocalDate.of(2025, 12, 1),
    )
  }

  @Test
  fun `should handle empty request`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(events = emptyList())

    val entitiesCaptor = argumentCaptor<List<EducationAssessmentEventEntity>>()
    given(educationAssessmentEventRepository.saveAll(entitiesCaptor.capture())).willReturn(emptyList())

    // When
    val response = controller.backfillAssessmentEvents(request)

    // Then
    assertThat(response.totalReceived).isEqualTo(0)
    assertThat(response.totalDeduplicatedInput).isEqualTo(0)
    assertThat(response.totalSaved).isEqualTo(0)

    assertThat(entitiesCaptor.firstValue).isEmpty()
  }

  @Test
  fun `should handle null detailUrl`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
          detailUrl = null,
        ),
      ),
    )

    val entitiesCaptor = argumentCaptor<List<EducationAssessmentEventEntity>>()
    given(educationAssessmentEventRepository.saveAll(entitiesCaptor.capture())).willReturn(emptyList())

    // When
    controller.backfillAssessmentEvents(request)

    // Then
    val savedEntities = entitiesCaptor.firstValue
    assertThat(savedEntities[0].detailUrl).isNull()
  }

  @Test
  fun `should generate unique references for each entity`() {
    // Given
    val request = EducationAssessmentBackfillController.BackfillRequest(
      events = listOf(
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "A1234BC",
          statusChangeDate = LocalDate.of(2025, 11, 15),
        ),
        EducationAssessmentBackfillController.BackfillEvent(
          prisonNumber = "D5678EF",
          statusChangeDate = LocalDate.of(2025, 11, 20),
        ),
      ),
    )

    val entitiesCaptor = argumentCaptor<List<EducationAssessmentEventEntity>>()
    given(educationAssessmentEventRepository.saveAll(entitiesCaptor.capture())).willReturn(emptyList())

    // When
    controller.backfillAssessmentEvents(request)

    // Then
    val savedEntities = entitiesCaptor.firstValue
    assertThat(savedEntities[0].reference).isNotEqualTo(savedEntities[1].reference)
  }
}
