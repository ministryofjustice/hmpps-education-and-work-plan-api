package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The various categories of work that a Prisoner may have worked in, or be interested in working in the future.
 */
enum class WorkType(val value: String) {

  @JsonProperty("OUTDOOR")
  OUTDOOR("OUTDOOR"),

  @JsonProperty("CONSTRUCTION")
  CONSTRUCTION("CONSTRUCTION"),

  @JsonProperty("DRIVING")
  DRIVING("DRIVING"),

  @JsonProperty("BEAUTY")
  BEAUTY("BEAUTY"),

  @JsonProperty("HOSPITALITY")
  HOSPITALITY("HOSPITALITY"),

  @JsonProperty("TECHNICAL")
  TECHNICAL("TECHNICAL"),

  @JsonProperty("MANUFACTURING")
  MANUFACTURING("MANUFACTURING"),

  @JsonProperty("OFFICE")
  OFFICE("OFFICE"),

  @JsonProperty("RETAIL")
  RETAIL("RETAIL"),

  @JsonProperty("SPORTS")
  SPORTS("SPORTS"),

  @JsonProperty("WAREHOUSING")
  WAREHOUSING("WAREHOUSING"),

  @JsonProperty("WASTE_MANAGEMENT")
  WASTE_MANAGEMENT("WASTE_MANAGEMENT"),

  @JsonProperty("EDUCATION_TRAINING")
  EDUCATION_TRAINING("EDUCATION_TRAINING"),

  @JsonProperty("CLEANING_AND_MAINTENANCE")
  CLEANING_AND_MAINTENANCE("CLEANING_AND_MAINTENANCE"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),
}
