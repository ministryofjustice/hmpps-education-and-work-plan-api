package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableAsync
class HmppsEducationAndWorkPlanApi

fun main(args: Array<String>) {
  runApplication<HmppsEducationAndWorkPlanApi>(*args)
}
