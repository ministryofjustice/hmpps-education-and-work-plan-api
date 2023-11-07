package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdateInductionDto

@ExtendWith(MockitoExtension::class)
class InductionServiceTest {

  @InjectMocks
  private lateinit var service: InductionService

  @Mock
  private lateinit var persistenceAdapter: InductionPersistenceAdapter

  companion object {
    private const val prisonNumber = "A1234AB"
  }

  @Test
  fun `should create induction`() {
    // Given
    val createInductionDto = aValidCreateInductionDto()
    given(persistenceAdapter.getInduction(any())).willReturn(null)

    // When
    service.createInduction(createInductionDto)

    // Then
    verify(persistenceAdapter).getInduction(prisonNumber)
    verify(persistenceAdapter).createInduction(createInductionDto)
  }

  @Test
  fun `should fail create induction given induction already exists`() {
    // Given
    val createInductionDto = aValidCreateInductionDto()
    val induction = aValidInduction()
    given(persistenceAdapter.getInduction(any())).willReturn(induction)

    // When
    val exception = catchThrowableOfType(
      { service.createInduction(createInductionDto) },
      InductionAlreadyExistsException::class.java,
    )

    // Then
    assertThat(exception.message).isEqualTo("An Induction already exists for prisoner $prisonNumber")
    verify(persistenceAdapter).getInduction(prisonNumber)
  }

  @Test
  fun `should get induction for prisoner`() {
    // Given
    val expected = aValidInduction()
    given(persistenceAdapter.getInduction(any())).willReturn(expected)

    // When
    val actual = service.getInductionForPrisoner(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(persistenceAdapter).getInduction(prisonNumber)
  }

  @Test
  fun `should fail to get induction for prisoner given induction does not exist`() {
    // Given
    given(persistenceAdapter.getInduction(any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.getInductionForPrisoner(prisonNumber) },
      InductionNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Induction not found for prisoner [$prisonNumber]")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    verify(persistenceAdapter).getInduction(prisonNumber)
  }

  @Test
  fun `should update induction`() {
    // Given
    val updateInductionDto = aValidUpdateInductionDto(prisonNumber = prisonNumber)
    val expected = aValidInduction(prisonNumber = prisonNumber)
    given(persistenceAdapter.updateInduction(any())).willReturn(expected)

    // When
    val actual = service.updateInduction(updateInductionDto)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(persistenceAdapter).updateInduction(updateInductionDto)
  }

  @Test
  fun `should fail to update induction given it does not exist`() {
    // Given
    val updateInductionDto = aValidUpdateInductionDto(prisonNumber = prisonNumber)
    given(persistenceAdapter.updateInduction(any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.updateInduction(updateInductionDto) },
      InductionNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Induction not found for prisoner [$prisonNumber]")
    verify(persistenceAdapter).updateInduction(updateInductionDto)
  }
}
