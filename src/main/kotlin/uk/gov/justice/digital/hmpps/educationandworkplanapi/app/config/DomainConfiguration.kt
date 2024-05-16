package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.domain.goal.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.domain.goal.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.domain.goal.service.GoalEventService
import uk.gov.justice.digital.hmpps.domain.goal.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.goal.service.GoalService
import uk.gov.justice.digital.hmpps.domain.timeline.service.PrisonTimelineService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelinePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService

/**
 * Configuration class responsible for providing domain bean implementations
 */
@Configuration
class DomainConfiguration {

  @Bean
  fun goalDomainService(
    goalPersistenceAdapter: GoalPersistenceAdapter,
    goalEventService: GoalEventService,
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
    actionPlanEventService: ActionPlanEventService,
  ): GoalService =
    GoalService(goalPersistenceAdapter, goalEventService, actionPlanPersistenceAdapter, actionPlanEventService)

  @Bean
  fun actionPlanDomainService(
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
    actionPlanEventService: ActionPlanEventService,
  ): ActionPlanService =
    ActionPlanService(actionPlanPersistenceAdapter, actionPlanEventService)

  @Bean
  fun timelineDomainService(
    timelinePersistenceAdapter: TimelinePersistenceAdapter,
    prisonTimelineService: PrisonTimelineService,
  ): TimelineService =
    TimelineService(timelinePersistenceAdapter, prisonTimelineService)

  @Bean
  fun inductionDomainService(
    inductionPersistenceAdapter: InductionPersistenceAdapter,
    inductionEventService: InductionEventService,
  ): InductionService =
    InductionService(inductionPersistenceAdapter, inductionEventService)
}
