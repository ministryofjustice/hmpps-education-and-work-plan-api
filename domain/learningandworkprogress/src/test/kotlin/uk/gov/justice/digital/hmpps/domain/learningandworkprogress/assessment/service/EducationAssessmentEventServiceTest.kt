package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.aValidEducationAssessmentEvent
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.dto.aValidCreateEducationAssessmentEventDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber

@ExtendWith(MockitoExtension::class)
class EducationAssessmentEventServiceTest {

  @Mock
  private lateinit var persistenceAdapter: EducationAssessmentEventPersistenceAdapter

  @Mock
  private lateinit var eventService: EducationAssessmentEventEventService

  @InjectMocks
  private lateinit var service: EducationAssessmentEventService

  @Nested
  inner class CreateEducationAssessmentEvent {

    @Test
    fun `should create education assessment event`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val dto = aValidCreateEducationAssessmentEventDto(prisonNumber = prisonNumber)

      val educationAssessmentEvent = aValidEducationAssessmentEvent(prisonNumber = prisonNumber)
      given(persistenceAdapter.createEducationAssessmentEvent(any())).willReturn(educationAssessmentEvent)

      // When
      val actual = service.createEducationAssessmentEvent(dto)

      // Then
      assertThat(actual).isEqualTo(educationAssessmentEvent)
      verify(persistenceAdapter).createEducationAssessmentEvent(dto)
      verify(eventService).educationAssessmentEventCreated(educationAssessmentEvent)
    }
  }

  @Nested
  inner class GetEducationAssessmentEvents {

    @Test
    fun `should get education assessment events for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val expected = listOf(aValidEducationAssessmentEvent(prisonNumber = prisonNumber))
      given(persistenceAdapter.getEducationAssessmentEvents(any())).willReturn(expected)

      // When
      val actual = service.getEducationAssessmentEvents(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(persistenceAdapter).getEducationAssessmentEvents(prisonNumber)
    }

    @Test
    fun `should return empty list given no education assessment events exist for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(persistenceAdapter.getEducationAssessmentEvents(any())).willReturn(emptyList())

      // When
      val actual = service.getEducationAssessmentEvents(prisonNumber)

      // Then
      assertThat(actual).isEmpty()
      verify(persistenceAdapter).getEducationAssessmentEvents(prisonNumber)
    }
  }
}
