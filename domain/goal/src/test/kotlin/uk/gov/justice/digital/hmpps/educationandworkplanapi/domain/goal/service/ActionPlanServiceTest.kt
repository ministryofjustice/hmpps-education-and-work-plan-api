package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanAlreadyExistsException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateActionPlanDto

@ExtendWith(MockitoExtension::class)
class ActionPlanServiceTest {
  @InjectMocks
  private lateinit var service: ActionPlanService

  @Mock
  private lateinit var persistenceAdapter: ActionPlanPersistenceAdapter

  @Mock
  private lateinit var actionPlanEventService: ActionPlanEventService

  @Nested
  inner class CreateActionPlan {
    @Test
    fun `should create action plan given prisoner does not already have an action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      given(persistenceAdapter.getActionPlan(any())).willReturn(null)

      val expectedActionPlan = aValidActionPlan(prisonNumber = prisonNumber)
      given(persistenceAdapter.createActionPlan(any())).willReturn(expectedActionPlan)

      val createActionPlanDto = aValidCreateActionPlanDto(prisonNumber = prisonNumber)

      // When
      val actual = service.createActionPlan(createActionPlanDto)

      // Then
      assertThat(actual).isEqualTo(expectedActionPlan)
      verify(persistenceAdapter).getActionPlan(prisonNumber)
      verify(persistenceAdapter).createActionPlan(createActionPlanDto)
      verify(actionPlanEventService).actionPlanCreated(expectedActionPlan)
    }

    @Test
    fun `should not create action plan given prisoner already has an action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      val actionPlan = aValidActionPlan(prisonNumber = prisonNumber)
      given(persistenceAdapter.getActionPlan(any())).willReturn(actionPlan)

      val createActionPlanDto = aValidCreateActionPlanDto(prisonNumber = prisonNumber)

      // When
      val exception = catchThrowableOfType(
        { service.createActionPlan(createActionPlanDto) },
        ActionPlanAlreadyExistsException::class.java,
      )

      // Then
      assertThat(exception)
        .hasMessage("An Action Plan already exists for prisoner $prisonNumber.")
      verify(persistenceAdapter).getActionPlan(prisonNumber)
      verifyNoInteractions(actionPlanEventService)
    }
  }

  @Nested
  inner class GetActionPlan {
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

    @Test
    fun `should fail to get action plan given no action plan exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      given(persistenceAdapter.getActionPlan(any())).willReturn(null)

      // When
      val retrievedActionPlan = service.getActionPlan(prisonNumber)

      // Then
      assertThat(retrievedActionPlan.prisonNumber).isEqualTo(prisonNumber)
      assertThat(retrievedActionPlan.goals).isEmpty()
      verify(persistenceAdapter).getActionPlan(prisonNumber)
    }
  }

  @Nested
  inner class GetActionPlanSummaries {
    @Test
    fun `should get action plan summaries`() {
      // Given
      val prisonNumbers = listOf(aValidPrisonNumber(), anotherValidPrisonNumber())

      val expectedActionPlanSummaries = listOf(
        aValidActionPlanSummary(prisonNumber = prisonNumbers[0]),
        aValidActionPlanSummary(prisonNumber = prisonNumbers[1]),
      )
      given(persistenceAdapter.getActionPlanSummaries(any())).willReturn(expectedActionPlanSummaries)

      // When
      val actual = service.getActionPlanSummaries(prisonNumbers)

      // Then
      assertThat(actual).isEqualTo(expectedActionPlanSummaries)
      verify(persistenceAdapter).getActionPlanSummaries(prisonNumbers)
    }
  }
}
