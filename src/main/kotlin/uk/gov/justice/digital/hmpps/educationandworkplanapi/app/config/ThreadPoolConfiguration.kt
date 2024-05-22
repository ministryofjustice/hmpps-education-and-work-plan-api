package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolConfiguration {
  @Bean("threadPoolTaskExecutor")
  fun taskExecutor(): TaskExecutor =
    ThreadPoolTaskExecutor().apply {
      corePoolSize = 200
      maxPoolSize = 1000
      this.setWaitForTasksToCompleteOnShutdown(true)
      threadNamePrefix = "Async-"
    }
}
