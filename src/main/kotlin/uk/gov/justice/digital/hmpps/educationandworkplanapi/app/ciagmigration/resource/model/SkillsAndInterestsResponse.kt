package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

/**
 * Represents the personal skills and interests of a Prisoner. This is a temporary class to allow us to import data from the CIAG API.
 */
data class SkillsAndInterestsResponse(

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who last updated this Prisoner's skills and interests.",
  )
  @get:JsonProperty("modifiedBy", required = true)
  val modifiedBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when this Prisoner's skills and interests was last updated. This will be the same as the created date if it has not yet been updated.",
  )
  @get:JsonProperty("modifiedDateTime", required = true)
  val modifiedDateTime: java.time.LocalDateTime,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", description = "One or more skills that the Prisoner feels they have.")
  @get:JsonProperty("skills")
  val skills: Set<PersonalSkill>? = null,

  @Schema(
    example = "null",
    description = "A specific type of a skill that the Prisoner feels they have. Mandatory when 'skills' includes 'OTHER'.",
  )
  @get:JsonProperty("skillsOther")
  val skillsOther: String? = null,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", description = "One or more interests that the Prisoner feels they have.")
  @get:JsonProperty("personalInterests")
  val personalInterests: Set<PersonalInterest>? = null,

  @Schema(
    example = "null",
    description = "A specific type of a interest that the Prisoner feels they have. Mandatory when 'personalInterests' includes 'OTHER'.",
  )
  @get:JsonProperty("personalInterestsOther")
  val personalInterestsOther: String? = null,
)
