package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.exception.ReturnAnErrorException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as GoalStatusDto

class GoalControllerTest {

  private val goalService = mock<GoalService>()
  private val goalResourceMapper = mock<GoalResourceMapper>()
  private val controller = GoalController(goalService, goalResourceMapper)

  private val prisonNumber = aValidPrisonNumber()

  @BeforeEach
  fun setUp() {
    given(goalResourceMapper.fromModelToDto(GoalStatus.ACTIVE)).willReturn(GoalStatusDto.ACTIVE)
    given(goalResourceMapper.fromModelToDto(GoalStatus.ARCHIVED)).willReturn(GoalStatusDto.ARCHIVED)
    given(goalResourceMapper.fromModelToDto(GoalStatus.COMPLETED)).willReturn(GoalStatusDto.COMPLETED)
  }

  @Test
  fun `should get goals successfully with no filter`() {
    val aValidGoalDto = aValidGoal()
    val aValidGoalResponse = aValidGoalResponse()
    given(goalService.getGoals(any())).willReturn(GetGoalsResult.Success(listOf(aValidGoalDto)))
    given(goalResourceMapper.fromDomainToModel(any<Goal>())).willReturn(aValidGoalResponse)
    given(goalResourceMapper.fromDomainToModel(any<GetGoalsResult.Success>())).willReturn(
      GetGoalsResponse(
        listOf(
          aValidGoalResponse,
        ),
      ),
    )

    val response = controller.getGoals(prisonNumber, null)
    assertThat(response).isEqualTo(GetGoalsResponse(listOf(aValidGoalResponse)))
    verify(goalService).getGoals(GetGoalsDto(prisonNumber, null))
  }

  @Test
  fun `should get goals successfully with a filter containing a single value`() {
    val aValidGoalDto = aValidGoal()
    val aValidGoalResponse = aValidGoalResponse()
    given(goalService.getGoals(any())).willReturn(GetGoalsResult.Success(listOf(aValidGoalDto)))
    given(goalResourceMapper.fromDomainToModel(any<Goal>())).willReturn(aValidGoalResponse)
    given(goalResourceMapper.fromDomainToModel(any<GetGoalsResult.Success>())).willReturn(
      GetGoalsResponse(
        listOf(
          aValidGoalResponse,
        ),
      ),
    )

    val response = controller.getGoals(prisonNumber, setOf(GoalStatus.ACTIVE))

    assertThat(response).isEqualTo(GetGoalsResponse(listOf(aValidGoalResponse)))
    verify(goalService).getGoals(GetGoalsDto(prisonNumber, setOf(GoalStatusDto.ACTIVE)))
  }

  @Test
  fun `should get goals successfully with a filter containing multiple values`() {
    val anActiveGoalDto = aValidGoal(status = GoalStatusDto.ACTIVE)
    val anArchivedGoalDto = aValidGoal(status = GoalStatusDto.ARCHIVED)
    given(goalService.getGoals(any())).willReturn(GetGoalsResult.Success(listOf(anActiveGoalDto, anArchivedGoalDto)))

    val anActiveGoalResponse = aValidGoalResponse(status = GoalStatus.ACTIVE)
    val anArchivedGoalResponse = aValidGoalResponse(status = GoalStatus.ARCHIVED)
    given(goalResourceMapper.fromDomainToModel(any<Goal>())).willReturn(anActiveGoalResponse, anArchivedGoalResponse)

    given(goalResourceMapper.fromDomainToModel(any<GetGoalsResult.Success>())).willReturn(
      GetGoalsResponse(
        listOf(
          anActiveGoalResponse,
          anArchivedGoalResponse,
        ),
      ),
    )

    val response = controller.getGoals(prisonNumber, setOf(GoalStatus.ACTIVE, GoalStatus.ARCHIVED))

    assertThat(response).isEqualTo(GetGoalsResponse(listOf(anActiveGoalResponse, anArchivedGoalResponse)))
    verify(goalService).getGoals(GetGoalsDto(prisonNumber, setOf(GoalStatusDto.ACTIVE, GoalStatusDto.ARCHIVED)))
  }

  @Test
  fun `should handle failed to get goals`() {
    given(goalService.getGoals(any())).willReturn(GetGoalsResult.PrisonerNotFound(prisonNumber))

    val exception = assertThrows(ReturnAnErrorException::class.java) {
      controller.getGoals(prisonNumber, null)
    }

    assertThat(exception.errorResponse).isEqualTo(
      ErrorResponse(
        status = 404,
        userMessage = "No goals have been created for prisoner [$prisonNumber] yet",
      ),
    )
    verify(goalService).getGoals(GetGoalsDto(prisonNumber, null))
  }
}
