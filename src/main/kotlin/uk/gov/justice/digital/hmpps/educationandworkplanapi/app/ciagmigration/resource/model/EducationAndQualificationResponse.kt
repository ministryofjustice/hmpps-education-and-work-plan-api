package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * A Prisoner's education and qualifications history. This is a temporary class to allow us to import data from the CIAG API.
 */
data class EducationAndQualificationResponse(

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who last updated this Prisoner's education and qualifications.",
  )
  @get:JsonProperty("modifiedBy", required = true)
  val modifiedBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when this Prisoner's education and qualifications was last updated. This will be the same as the created date if it has not yet been updated.",
  )
  @get:JsonProperty("modifiedDateTime", required = true)
  val modifiedDateTime: java.time.LocalDateTime,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("educationLevel")
  val educationLevel: HighestEducationLevel? = null,

  @field:Valid
  @Schema(example = "null", description = "A list of the Prisoner's previous qualifications")
  @get:JsonProperty("qualifications")
  val qualifications: Set<AchievedQualification>? = null,

  @field:Valid
  @Schema(example = "null", description = "Any additional training that the Prisoner has completed in the past.")
  @get:JsonProperty("additionalTraining")
  val additionalTraining: Set<TrainingType>? = null,

  @Schema(
    example = "null",
    description = "A specific type of training that does not fit the given 'additionalTraining' types. Mandatory when 'additionalTraining' includes 'OTHER'.",
  )
  @get:JsonProperty("additionalTrainingOther")
  val additionalTrainingOther: String? = null,
)
