package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Whether the Prisoner hopes to get work.
 */
enum class HopingToWork(val value: String) {

  @JsonProperty("YES")
  YES("YES"),

  @JsonProperty("NO")
  NO("NO"),

  @JsonProperty("NOT_SURE")
  NOT_SURE("NOT_SURE"),
}
