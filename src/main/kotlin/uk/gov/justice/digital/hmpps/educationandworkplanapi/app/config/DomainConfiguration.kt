package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalServiceDecorator

/**
 * Configuration class responsible for providing domain bean implementations
 */
@Configuration
class DomainConfiguration {

  @Bean
  fun goalDomainService(
    goalPersistenceAdapter: GoalPersistenceAdapter,
    goalServiceDecorator: GoalServiceDecorator,
  ): GoalService =
    GoalService(goalPersistenceAdapter, goalServiceDecorator)

  @Bean
  fun actionPlanDomainService(
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  ): ActionPlanService =
    ActionPlanService(actionPlanPersistenceAdapter)
}
