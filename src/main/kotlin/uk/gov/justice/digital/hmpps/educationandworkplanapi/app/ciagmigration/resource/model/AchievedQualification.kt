package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * A qualification that a Prisoner has achieved.
 */
data class AchievedQualification(

  @Schema(example = "null", description = "The subject of the qualification.")
  @get:JsonProperty("subject")
  val subject: String? = null,

  @Schema(example = "null", description = "The level of the qualification (if known/relevant).")
  @get:JsonProperty("level")
  val level: Level? = null,

  @Schema(example = "null", description = "The grade which was achieved (if known/relevant).")
  @get:JsonProperty("grade")
  val grade: String? = null,
) {

  /**
   * The level of the qualification (if known/relevant).
   * Values: ENTRY_LEVEL,LEVEL_1,LEVEL_2,LEVEL_3,LEVEL_4,LEVEL_5,LEVEL_6,LEVEL_7,LEVEL_8
   */
  enum class Level(val value: String) {

    @JsonProperty("ENTRY_LEVEL")
    ENTRY_LEVEL("ENTRY_LEVEL"),

    @JsonProperty("LEVEL_1")
    LEVEL_1("LEVEL_1"),

    @JsonProperty("LEVEL_2")
    LEVEL_2("LEVEL_2"),

    @JsonProperty("LEVEL_3")
    LEVEL_3("LEVEL_3"),

    @JsonProperty("LEVEL_4")
    LEVEL_4("LEVEL_4"),

    @JsonProperty("LEVEL_5")
    LEVEL_5("LEVEL_5"),

    @JsonProperty("LEVEL_6")
    LEVEL_6("LEVEL_6"),

    @JsonProperty("LEVEL_7")
    LEVEL_7("LEVEL_7"),

    @JsonProperty("LEVEL_8")
    LEVEL_8("LEVEL_8"),
  }
}
