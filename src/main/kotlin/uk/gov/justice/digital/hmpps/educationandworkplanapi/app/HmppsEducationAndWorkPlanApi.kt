package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsEducationAndWorkPlanApi

fun main(args: Array<String>) {
  runApplication<HmppsEducationAndWorkPlanApi>(*args)
}
