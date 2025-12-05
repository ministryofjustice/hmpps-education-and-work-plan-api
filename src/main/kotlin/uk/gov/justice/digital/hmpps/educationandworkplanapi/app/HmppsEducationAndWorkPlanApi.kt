package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.InductionExtensionConfig

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableAsync
@EnableConfigurationProperties(InductionExtensionConfig::class)
class HmppsEducationAndWorkPlanApi

fun main(args: Array<String>) {
  runApplication<HmppsEducationAndWorkPlanApi>(*args)
}
