package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import kotlinx.coroutines.test.runTest
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
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInductionDto

@ExtendWith(MockitoExtension::class)
class InductionServiceTest {

  @InjectMocks
  private lateinit var service: InductionService

  @Mock
  private lateinit var persistenceAdapter: InductionPersistenceAdapter

  @Mock
  private lateinit var inductionEventService: InductionEventService

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
  }

  @Nested
  inner class CreateInduction {
    @Test
    fun `should create induction`() = runTest {
      // Given
      val createInductionDto = aValidCreateInductionDto()
      val induction = aFullyPopulatedInduction()
      given(persistenceAdapter.getInduction(any())).willReturn(null)
      given(persistenceAdapter.createInduction(any())).willReturn(induction)

      // When
      service.createInduction(createInductionDto)

      // Then
      verify(persistenceAdapter).getInduction(PRISON_NUMBER)
      verify(persistenceAdapter).createInduction(createInductionDto)
      verify(inductionEventService).inductionCreated(induction)
    }

    @Test
    fun `should fail create induction given induction already exists`() {
      // Given
      val createInductionDto = aValidCreateInductionDto()
      val induction = aFullyPopulatedInduction()
      given(persistenceAdapter.getInduction(any())).willReturn(induction)

      // When
      val exception = catchThrowableOfType(
        { runTest { service.createInduction(createInductionDto) } },
        InductionAlreadyExistsException::class.java,
      )

      // Then
      assertThat(exception.message).isEqualTo("An Induction already exists for prisoner $PRISON_NUMBER")
      verify(persistenceAdapter).getInduction(PRISON_NUMBER)
      verifyNoInteractions(inductionEventService)
    }
  }

  @Nested
  inner class GetInductionForPrisoner {
    @Test
    fun `should get induction for prisoner`() {
      // Given
      val expected = aFullyPopulatedInduction()
      given(persistenceAdapter.getInduction(any())).willReturn(expected)

      // When
      val actual = service.getInductionForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(persistenceAdapter).getInduction(PRISON_NUMBER)
    }

    @Test
    fun `should fail to get induction for prisoner given induction does not exist`() {
      // Given
      given(persistenceAdapter.getInduction(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(
        { service.getInductionForPrisoner(PRISON_NUMBER) },
        InductionNotFoundException::class.java,
      )

      // Then
      assertThat(exception).hasMessage("Induction not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(persistenceAdapter).getInduction(PRISON_NUMBER)
    }
  }

  @Nested
  inner class UpdateInduction {
    @Test
    fun `should update induction`() = runTest {
      // Given
      val updateInductionDto = aValidUpdateInductionDto(prisonNumber = PRISON_NUMBER)
      val expected = aFullyPopulatedInduction(prisonNumber = PRISON_NUMBER)
      given(persistenceAdapter.updateInduction(any())).willReturn(expected)

      // When
      val actual = service.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(persistenceAdapter).updateInduction(updateInductionDto)
      verify(inductionEventService).inductionUpdated(expected)
    }

    @Test
    fun `should fail to update induction given it does not exist`() {
      // Given
      val updateInductionDto = aValidUpdateInductionDto(prisonNumber = PRISON_NUMBER)
      given(persistenceAdapter.updateInduction(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(
        { runTest { service.updateInduction(updateInductionDto) } },
        InductionNotFoundException::class.java,
      )

      // Then
      assertThat(exception).hasMessage("Induction not found for prisoner [$PRISON_NUMBER]")
      verify(persistenceAdapter).updateInduction(updateInductionDto)
      verifyNoInteractions(inductionEventService)
    }
  }

  @Nested
  inner class GetInductionSummaries {
    @Test
    fun `should get induction summaries given one or more prison numbers`() {
      // Given
      val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())

      val expectedInductionSummaries = listOf(
        aValidInductionSummary(prisonNumber = prisonNumbers[0]),
        aValidInductionSummary(prisonNumber = prisonNumbers[1]),
      )
      given(persistenceAdapter.getInductionSummaries(any())).willReturn(expectedInductionSummaries)

      // When
      val actual = service.getInductionSummaries(prisonNumbers)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSummaries)
      verify(persistenceAdapter).getInductionSummaries(prisonNumbers)
    }

    @Test
    fun `should get induction summaries given no prison numbers`() {
      // Given
      val prisonNumbers: List<String> = emptyList()

      val expectedInductionSummaries: List<InductionSummary> = emptyList()

      // When
      val actual = service.getInductionSummaries(prisonNumbers)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSummaries)
      verifyNoInteractions(persistenceAdapter)
    }
  }
}
