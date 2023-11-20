package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Types of work that can be done whilst in prison.
 */
enum class InPrisonWorkType(val value: String) {

  @JsonProperty("CLEANING_AND_HYGIENE")
  CLEANING_AND_HYGIENE("CLEANING_AND_HYGIENE"),

  @JsonProperty("COMPUTERS_OR_DESK_BASED")
  COMPUTERS_OR_DESK_BASED("COMPUTERS_OR_DESK_BASED"),

  @JsonProperty("GARDENING_AND_OUTDOORS")
  GARDENING_AND_OUTDOORS("GARDENING_AND_OUTDOORS"),

  @JsonProperty("KITCHENS_AND_COOKING")
  KITCHENS_AND_COOKING("KITCHENS_AND_COOKING"),

  @JsonProperty("MAINTENANCE")
  MAINTENANCE("MAINTENANCE"),

  @JsonProperty("PRISON_LAUNDRY")
  PRISON_LAUNDRY("PRISON_LAUNDRY"),

  @JsonProperty("PRISON_LIBRARY")
  PRISON_LIBRARY("PRISON_LIBRARY"),

  @JsonProperty("TEXTILES_AND_SEWING")
  TEXTILES_AND_SEWING("TEXTILES_AND_SEWING"),

  @JsonProperty("WELDING_AND_METALWORK")
  WELDING_AND_METALWORK("WELDING_AND_METALWORK"),

  @JsonProperty("WOODWORK_AND_JOINERY")
  WOODWORK_AND_JOINERY("WOODWORK_AND_JOINERY"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),
}
