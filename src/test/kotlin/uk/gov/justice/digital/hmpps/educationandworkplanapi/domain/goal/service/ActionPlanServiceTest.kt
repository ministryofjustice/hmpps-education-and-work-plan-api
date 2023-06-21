package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan

@ExtendWith(MockitoExtension::class)
class ActionPlanServiceTest {
  @InjectMocks
  private lateinit var service: ActionPlanService

  @Mock
  private lateinit var persistenceAdapter: ActionPlanPersistenceAdapter

  @Test
  fun `should retrieve action plan by prison number`() {
    // Given
    val actionPlan = aValidActionPlan()
    val prisonNumber = aValidPrisonNumber()
    given(persistenceAdapter.getActionPlan(any())).willReturn(actionPlan)

    // When
    val retrievedActionPlan = service.getActionPlan(prisonNumber)

    // Then
    assertThat(retrievedActionPlan).isEqualTo(actionPlan)
    verify(persistenceAdapter).getActionPlan(prisonNumber)
  }
}
