package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a Prisoner's personal skill.
 */
enum class PersonalSkill(val value: String) {

  @JsonProperty("COMMUNICATION")
  COMMUNICATION("COMMUNICATION"),

  @JsonProperty("POSITIVE_ATTITUDE")
  POSITIVE_ATTITUDE("POSITIVE_ATTITUDE"),

  @JsonProperty("RESILIENCE")
  RESILIENCE("RESILIENCE"),

  @JsonProperty("SELF_MANAGEMENT")
  SELF_MANAGEMENT("SELF_MANAGEMENT"),

  @JsonProperty("TEAMWORK")
  TEAMWORK("TEAMWORK"),

  @JsonProperty("THINKING_AND_PROBLEM_SOLVING")
  THINKING_AND_PROBLEM_SOLVING("THINKING_AND_PROBLEM_SOLVING"),

  @JsonProperty("WILLINGNESS_TO_LEARN")
  WILLINGNESS_TO_LEARN("WILLINGNESS_TO_LEARN"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),

  @JsonProperty("NONE")
  NONE("NONE"),
}
