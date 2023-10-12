package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction

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
    val induction = aValidInduction()

    // When
    service.createInduction(prisonNumber, induction)

    // Then
    verify(persistenceAdapter).saveInduction(prisonNumber, induction)
  }
}
