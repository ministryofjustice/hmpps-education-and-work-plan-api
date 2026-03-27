package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

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
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber

@ExtendWith(MockitoExtension::class)
class ActionPlanServiceTest {
  @InjectMocks
  private lateinit var service: ActionPlanService

  @Mock
  private lateinit var persistenceAdapter: ActionPlanPersistenceAdapter

  @Mock
  private lateinit var actionPlanEventService: ActionPlanEventService

  @Mock
  private lateinit var goalNotesService: GoalNotesService

  @Nested
  inner class CreateActionPlan {
    @Test
    fun `should create action plan given prisoner does not already have an action plan`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()

      val actionPlan = aValidActionPlan(prisonNumber = prisonNumber)
      given(persistenceAdapter.getActionPlan(any())).willReturn(actionPlan)

      val createActionPlanDto = aValidCreateActionPlanDto(prisonNumber = prisonNumber)

      // When
      val exception = catchThrowableOfType(ActionPlanAlreadyExistsException::class.java) {
        service.createActionPlan(createActionPlanDto)
      }

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
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
      given(persistenceAdapter.getActionPlan(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(ActionPlanNotFoundException::class.java) {
        service.getActionPlan(prisonNumber)
      }

      // Then
      assertThat(exception)
        .hasMessage("ActionPlan for prisoner [$prisonNumber] not found")
      verify(persistenceAdapter).getActionPlan(prisonNumber)
    }
  }
}
