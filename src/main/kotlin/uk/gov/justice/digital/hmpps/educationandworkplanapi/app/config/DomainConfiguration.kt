package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.DefaultActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.DefaultGoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService

/**
 * Configuration class responsible for providing domain bean implementations
 */
@Configuration
class DomainConfiguration {

  @Bean("defaultDomainGoalService")
  fun goalDomainService(
    goalPersistenceAdapter: GoalPersistenceAdapter,
  ): GoalService =
    DefaultGoalService(goalPersistenceAdapter)

  @Bean("defaultDomainActionPlanService")
  fun actionPlanDomainService(
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  ): ActionPlanService =
    DefaultActionPlanService(actionPlanPersistenceAdapter)
}
