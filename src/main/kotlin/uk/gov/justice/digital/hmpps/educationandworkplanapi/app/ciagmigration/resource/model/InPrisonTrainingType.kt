package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The types of training that can be done whilst in prison.
 */
enum class InPrisonTrainingType(val value: String) {

  @JsonProperty("BARBERING_AND_HAIRDRESSING")
  BARBERING_AND_HAIRDRESSING("BARBERING_AND_HAIRDRESSING"),

  @JsonProperty("CATERING")
  CATERING("CATERING"),

  @JsonProperty("COMMUNICATION_SKILLS")
  COMMUNICATION_SKILLS("COMMUNICATION_SKILLS"),

  @JsonProperty("ENGLISH_LANGUAGE_SKILLS")
  ENGLISH_LANGUAGE_SKILLS("ENGLISH_LANGUAGE_SKILLS"),

  @JsonProperty("FORKLIFT_DRIVING")
  FORKLIFT_DRIVING("FORKLIFT_DRIVING"),

  @JsonProperty("INTERVIEW_SKILLS")
  INTERVIEW_SKILLS("INTERVIEW_SKILLS"),

  @JsonProperty("MACHINERY_TICKETS")
  MACHINERY_TICKETS("MACHINERY_TICKETS"),

  @JsonProperty("NUMERACY_SKILLS")
  NUMERACY_SKILLS("NUMERACY_SKILLS"),

  @JsonProperty("RUNNING_A_BUSINESS")
  RUNNING_A_BUSINESS("RUNNING_A_BUSINESS"),

  @JsonProperty("SOCIAL_AND_LIFE_SKILLS")
  SOCIAL_AND_LIFE_SKILLS("SOCIAL_AND_LIFE_SKILLS"),

  @JsonProperty("WELDING_AND_METALWORK")
  WELDING_AND_METALWORK("WELDING_AND_METALWORK"),

  @JsonProperty("WOODWORK_AND_JOINERY")
  WOODWORK_AND_JOINERY("WOODWORK_AND_JOINERY"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),
}
