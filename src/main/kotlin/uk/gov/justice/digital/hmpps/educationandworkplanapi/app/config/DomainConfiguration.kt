package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelinePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

/**
 * Configuration class responsible for providing domain bean implementations
 */
@Configuration
class DomainConfiguration {

  @Bean
  fun goalDomainService(
    goalPersistenceAdapter: GoalPersistenceAdapter,
    goalEventService: GoalEventService,
  ): GoalService =
    GoalService(goalPersistenceAdapter, goalEventService)

  @Bean
  fun actionPlanDomainService(
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
    actionPlanEventService: ActionPlanEventService,
  ): ActionPlanService =
    ActionPlanService(actionPlanPersistenceAdapter, actionPlanEventService)

  @Bean
  fun timelineDomainService(timelinePersistenceAdapter: TimelinePersistenceAdapter): TimelineService =
    TimelineService(timelinePersistenceAdapter)
}
