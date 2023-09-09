package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalServiceDecorator
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.NoopGoalServiceDecorator

/**
 * Implementation of [GoalServiceDecorator] that decorates Goal Service methods with additional behaviours that are
 * invoked asynchronously.
 */
@Component
class AsyncGoalServiceDecorator(private val telemetryService: TelemetryService) : NoopGoalServiceDecorator() {

  override fun afterCreateGoal(prisonNumber: String, createGoalDto: CreateGoalDto, createdGoal: Goal) {
    runBlocking {
      launch {
        telemetryService.trackGoalCreateEvent(createdGoal)
      }
    }
  }

  override fun afterUpdateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto, updatedGoal: Goal) {
    runBlocking {
      launch {
        telemetryService.trackGoalUpdateEvent(updatedGoal)
      }
    }
  }
}
