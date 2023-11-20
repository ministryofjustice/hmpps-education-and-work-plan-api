package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Additional training types that a Prisoner may have done.
 */
enum class TrainingType(val value: String) {

  @JsonProperty("CSCS_CARD")
  CSCS_CARD("CSCS_CARD"),

  @JsonProperty("FIRST_AID_CERTIFICATE")
  FIRST_AID_CERTIFICATE("FIRST_AID_CERTIFICATE"),

  @JsonProperty("FOOD_HYGIENE_CERTIFICATE")
  FOOD_HYGIENE_CERTIFICATE("FOOD_HYGIENE_CERTIFICATE"),

  @JsonProperty("FULL_UK_DRIVING_LICENCE")
  FULL_UK_DRIVING_LICENCE("FULL_UK_DRIVING_LICENCE"),

  @JsonProperty("HEALTH_AND_SAFETY")
  HEALTH_AND_SAFETY("HEALTH_AND_SAFETY"),

  @JsonProperty("HGV_LICENCE")
  HGV_LICENCE("HGV_LICENCE"),

  @JsonProperty("MACHINERY_TICKETS")
  MACHINERY_TICKETS("MACHINERY_TICKETS"),

  @JsonProperty("MANUAL_HANDLING")
  MANUAL_HANDLING("MANUAL_HANDLING"),

  @JsonProperty("TRADE_COURSE")
  TRADE_COURSE("TRADE_COURSE"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),

  @JsonProperty("NONE")
  NONE("NONE"),
}
