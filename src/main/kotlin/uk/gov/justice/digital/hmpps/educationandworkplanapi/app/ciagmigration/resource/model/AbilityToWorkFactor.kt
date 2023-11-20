package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Factors affecting a prisoner's ability to work.
 */
enum class AbilityToWorkFactor(val value: String) {

  @JsonProperty("CARING_RESPONSIBILITIES")
  CARING_RESPONSIBILITIES("CARING_RESPONSIBILITIES"),

  @JsonProperty("LIMITED_BY_OFFENSE")
  LIMITED_BY_OFFENSE("LIMITED_BY_OFFENSE"),

  @JsonProperty("HEALTH_ISSUES")
  HEALTH_ISSUES("HEALTH_ISSUES"),

  @JsonProperty("NO_RIGHT_TO_WORK")
  NO_RIGHT_TO_WORK("NO_RIGHT_TO_WORK"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),

  @JsonProperty("NONE")
  NONE("NONE"),
}
