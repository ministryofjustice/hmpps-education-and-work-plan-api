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
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications

@ExtendWith(MockitoExtension::class)
class EducationServiceTest {

  @Mock
  private lateinit var persistenceAdapter: EducationPersistenceAdapter

  @InjectMocks
  private lateinit var educationService: EducationService

  @Nested
  inner class GetPreviousQualificationsForPrisoner {

    @Test
    fun `should get previous qualifications for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      val expected = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(expected)

      // When
      val actual = educationService.getPreviousQualificationsForPrisoner(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
    }

    @Test
    fun `should throw exception given previous qualifications record for prisoner does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      given(persistenceAdapter.getPreviousQualifications(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(EducationNotFoundException::class.java) {
        educationService.getPreviousQualificationsForPrisoner(prisonNumber)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(persistenceAdapter).getPreviousQualifications(prisonNumber)
    }
  }
}
