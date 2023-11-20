package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Reasons the Prisoner has given not to get work after leaving Prison.
 */
enum class ReasonNotToWork(val value: String) {

  @JsonProperty("LIMIT_THEIR_ABILITY")
  LIMIT_THEIR_ABILITY("LIMIT_THEIR_ABILITY"),

  @JsonProperty("FULL_TIME_CARER")
  FULL_TIME_CARER("FULL_TIME_CARER"),

  @JsonProperty("LACKS_CONFIDENCE_OR_MOTIVATION")
  LACKS_CONFIDENCE_OR_MOTIVATION("LACKS_CONFIDENCE_OR_MOTIVATION"),

  @JsonProperty("HEALTH")
  HEALTH("HEALTH"),

  @JsonProperty("RETIRED")
  RETIRED("RETIRED"),

  @JsonProperty("NO_RIGHT_TO_WORK")
  NO_RIGHT_TO_WORK("NO_RIGHT_TO_WORK"),

  @JsonProperty("NOT_SURE")
  NOT_SURE("NOT_SURE"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),

  @JsonProperty("NO_REASON")
  NO_REASON("NO_REASON"),
}
