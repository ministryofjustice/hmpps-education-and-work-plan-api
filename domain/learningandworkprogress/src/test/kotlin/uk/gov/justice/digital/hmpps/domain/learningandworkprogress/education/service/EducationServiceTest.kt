package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber

@ExtendWith(MockitoExtension::class)
class EducationServiceTest {

  @Mock
  private lateinit var persistenceAdapter: EducationPersistenceAdapter

  @Mock
  private lateinit var educationEventService: EducationEventService

  @InjectMocks
  private lateinit var educationService: EducationService

  @Nested
  inner class GetPreviousQualificationsForPrisoner {

    @Test
    fun `should get previous qualifications for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val expected = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(expected)

      // When
      val actual = educationService.getPreviousQualificationsForPrisoner(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
      verifyNoInteractions(educationEventService)
    }

    @Test
    fun `should throw exception given previous qualifications record for prisoner does not exist`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(EducationNotFoundException::class.java) {
        educationService.getPreviousQualificationsForPrisoner(prisonNumber)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
      verifyNoInteractions(educationEventService)
    }
  }

  @Nested
  inner class CreatePreviousQualifications {

    @Test
    fun `should create previous qualifications for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val createPreviousQualificationsDto = aValidCreatePreviousQualificationsDto(prisonNumber = prisonNumber)

      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(null)

      val previousQualifications = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(persistenceAdapter.createPreviousQualifications(any())).willReturn(previousQualifications)

      // When
      val actual = educationService.createPreviousQualifications(createPreviousQualificationsDto)

      // Then
      assertThat(actual).isEqualTo(previousQualifications)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
      verify(persistenceAdapter).createPreviousQualifications(createPreviousQualificationsDto)
      verify(educationEventService).previousQualificationsCreated(previousQualifications)
    }

    @Test
    fun `should throw exception given previous qualifications record for prisoner already exists`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val createPreviousQualificationsDto = aValidCreatePreviousQualificationsDto(prisonNumber = prisonNumber)

      val previousQualifications = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(previousQualifications)

      // When
      val exception = catchThrowableOfType(EducationAlreadyExistsException::class.java) {
        educationService.createPreviousQualifications(createPreviousQualificationsDto)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
      verifyNoMoreInteractions(persistenceAdapter)
      verifyNoInteractions(educationEventService)
    }
  }

  @Nested
  inner class UpdatePreviousQualifications {
    @Test
    fun `should update previous qualifications for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val updatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(prisonNumber = prisonNumber)

      val previousQualifications = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(persistenceAdapter.updatePreviousQualifications(any())).willReturn(previousQualifications)

      // When
      val actual = educationService.updatePreviousQualifications(updatePreviousQualificationsDto)

      // Then
      assertThat(actual).isEqualTo(previousQualifications)
      verify(persistenceAdapter).updatePreviousQualifications(updatePreviousQualificationsDto)
      verify(educationEventService).previousQualificationsUpdated(previousQualifications)
    }

    @Test
    fun `should throw exception given previous qualifications record for prisoner does not exist`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val updatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(prisonNumber = prisonNumber)

      given(persistenceAdapter.updatePreviousQualifications(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(EducationNotFoundException::class.java) {
        educationService.updatePreviousQualifications(updatePreviousQualificationsDto)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(persistenceAdapter).updatePreviousQualifications(updatePreviousQualificationsDto)
      verifyNoInteractions(educationEventService)
    }
  }
}
