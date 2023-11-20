package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

/**
 * A Prisoner's previous work experience. This is a temporary class to allow us to import data from the CIAG API.
 */
data class PreviousWorkResponse(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hasWorkedBefore", required = true)
  val hasWorkedBefore: Boolean,

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who last updated this Prisoner's previous work.",
  )
  @get:JsonProperty("modifiedBy", required = true)
  val modifiedBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when this Prisoner's previous work was last updated. This will be the same as the created date if it has not yet been updated.",
  )
  @get:JsonProperty("modifiedDateTime", required = true)
  val modifiedDateTime: java.time.OffsetDateTime,

  @Schema(
    example = "c88a6c48-97e2-4c04-93b5-98619966447b",
    description = "A unique reference for this Prisoner's previous work experience (not the database primary key).",
  )
  @get:JsonProperty("id")
  val id: Integer,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", description = "A list of the Prisoner's type of previous work experience.")
  @get:JsonProperty("typeOfWorkExperience")
  val typeOfWorkExperience: Set<WorkType>? = null,

  @Schema(
    example = "null",
    description = "A specific work experience type for the Prisoner. Mandatory when 'typeOfWorkExperience' includes 'OTHER'.",
  )
  @get:JsonProperty("typeOfWorkExperienceOther")
  val typeOfWorkExperienceOther: String? = null,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", description = "A list of the Prisoner's previous work experience details.")
  @get:JsonProperty("workExperience")
  val workExperience: Set<WorkExperience>? = null,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("workInterests")
  val workInterests: WorkInterestsResponse? = null,
)
