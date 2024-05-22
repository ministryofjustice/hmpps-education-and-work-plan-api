package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineScopes {
  // A scope with its own SupervisorJob() is not linked to its parent scope - so there is no structured concurrency and
  // this scope has its own lifecycle. If a child coroutine fails, it does not affect the parent or sibling coroutines.
  @Bean
  fun fireAndForgetScope() = CoroutineScope(SupervisorJob())
}
