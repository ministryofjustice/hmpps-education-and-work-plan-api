package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import java.util.UUID

/**
 * Implementation of [GoalService] that wraps the default domain Goal Service methods to allow for
 * invoking additional behaviours.
 */
@Component("domainGoalServiceWrapper")
class DomainGoalServiceWrapper(
  @Qualifier("defaultDomainGoalService") private val wrappedGoalService: GoalService,
  private val telemetryService: TelemetryService,
) : GoalService {

  override fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal = runBlocking {
    wrappedGoalService.createGoal(prisonNumber, createGoalDto).also {
      launch {
        telemetryService.trackGoalCreateEvent(it)
      }
    }
  }

  override fun getGoal(prisonNumber: String, goalReference: UUID): Goal =
    wrappedGoalService.getGoal(prisonNumber, goalReference)

  override fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal = runBlocking {
    wrappedGoalService.updateGoal(prisonNumber, updatedGoalDto).also {
      launch {
        telemetryService.trackGoalUpdateEvent(it)
      }
    }
  }
}
