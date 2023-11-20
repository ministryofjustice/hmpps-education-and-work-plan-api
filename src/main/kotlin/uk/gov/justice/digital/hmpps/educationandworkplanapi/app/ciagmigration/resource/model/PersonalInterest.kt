package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a Prisoner's personal interest.
 */
enum class PersonalInterest(val value: String) {

  @JsonProperty("COMMUNITY")
  COMMUNITY("COMMUNITY"),

  @JsonProperty("CRAFTS")
  CRAFTS("CRAFTS"),

  @JsonProperty("CREATIVE")
  CREATIVE("CREATIVE"),

  @JsonProperty("DIGITAL")
  DIGITAL("DIGITAL"),

  @JsonProperty("KNOWLEDGE_BASED")
  KNOWLEDGE_BASED("KNOWLEDGE_BASED"),

  @JsonProperty("MUSICAL")
  MUSICAL("MUSICAL"),

  @JsonProperty("OUTDOOR")
  OUTDOOR("OUTDOOR"),

  @JsonProperty("NATURE_AND_ANIMALS")
  NATURE_AND_ANIMALS("NATURE_AND_ANIMALS"),

  @JsonProperty("SOCIAL")
  SOCIAL("SOCIAL"),

  @JsonProperty("SOLO_ACTIVITIES")
  SOLO_ACTIVITIES("SOLO_ACTIVITIES"),

  @JsonProperty("SOLO_SPORTS")
  SOLO_SPORTS("SOLO_SPORTS"),

  @JsonProperty("TEAM_SPORTS")
  TEAM_SPORTS("TEAM_SPORTS"),

  @JsonProperty("WELLNESS")
  WELLNESS("WELLNESS"),

  @JsonProperty("OTHER")
  OTHER("OTHER"),

  @JsonProperty("NONE")
  NONE("NONE"),
}
