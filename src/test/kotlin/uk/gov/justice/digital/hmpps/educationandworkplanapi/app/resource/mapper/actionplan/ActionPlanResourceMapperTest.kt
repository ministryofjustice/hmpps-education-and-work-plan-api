package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class ActionPlanResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: ActionPlanResourceMapper

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @Test
  fun `should map from model to DTO`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val request = aValidCreateActionPlanRequest()
    val expectedCreateGoalDto = aValidCreateGoalDto()
    val expectedCreateActionPlanDto = aValidCreateActionPlanDto(
      prisonNumber = prisonNumber,
      goals = listOf(expectedCreateGoalDto),
    )
    given(goalMapper.fromModelToDto(any<CreateGoalRequest>())).willReturn(expectedCreateGoalDto)

    // When
    val actual = mapper.fromModelToDto(prisonNumber, request)

    // Then
    assertThat(actual).isEqualTo(expectedCreateActionPlanDto)
    verify(goalMapper).fromModelToDto(request.goals[0])
  }

  @Test
  fun `should map from domain to model given there are goal notes for the goal`() {
    // Given
    val goal = aValidGoal()
    val actionPlan = aValidActionPlan(goals = listOf(goal))

    val goalNotes = listOf(
      aValidNoteDto(
        entityType = EntityType.GOAL,
        noteType = NoteType.GOAL,
        entityReference = goal.reference,
      ),
    )
    val goalNotesByGoalReference = mapOf(
      goal.reference to goalNotes,
    )

    val expectedGoal = aValidGoalResponse(
      goalNotes = listOf(aValidNoteResponse()),
    )
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoal)

    val expectedActionPlan = aValidActionPlanResponse(
      reference = actionPlan.reference,
      prisonNumber = actionPlan.prisonNumber,
      goals = mutableListOf(expectedGoal),
    )

    // When
    val actual = mapper.fromDomainToModel(actionPlan, goalNotesByGoalReference)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedActionPlan)
    verify(goalMapper).fromDomainToModel(actionPlan.goals[0], goalNotes)
  }

  @Test
  fun `should map from domain to model given there are no goal notes for the goal`() {
    // Given
    val goal = aValidGoal()
    val actionPlan = aValidActionPlan(goals = listOf(goal))

    val goalNotesByGoalReference = mapOf<UUID, List<NoteDto>>(
      goal.reference to emptyList(),
    )

    val expectedGoal = aValidGoalResponse(goalNotes = emptyList())
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoal)

    val expectedActionPlan = aValidActionPlanResponse(
      reference = actionPlan.reference,
      prisonNumber = actionPlan.prisonNumber,
      goals = mutableListOf(expectedGoal),
    )

    // When
    val actual = mapper.fromDomainToModel(actionPlan, goalNotesByGoalReference)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedActionPlan)
    verify(goalMapper).fromDomainToModel(actionPlan.goals[0], emptyList())
  }

  @Test
  fun `should map from domain summaries to model summaries`() {
    // Given
    val summary1 = aValidActionPlanSummary(prisonNumber = aValidPrisonNumber())
    val summary2 = aValidActionPlanSummary(prisonNumber = anotherValidPrisonNumber())
    val expected = listOf(
      aValidActionPlanSummaryResponse(
        prisonNumber = summary1.prisonNumber,
        reference = summary1.reference,
      ),
      aValidActionPlanSummaryResponse(
        prisonNumber = summary2.prisonNumber,
        reference = summary2.reference,
      ),
    )

    // When
    val actual = mapper.fromDomainToModel(listOf(summary1, summary2))

    // Then
    assertThat(actual).hasSize(2)
    assertThat(actual).isEqualTo(expected)
  }
}
