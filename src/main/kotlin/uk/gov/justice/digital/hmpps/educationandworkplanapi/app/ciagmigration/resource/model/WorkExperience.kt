package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * A Prisoner's list of work experience details. This is a temporary class to allow us to import data from the CIAG API.
 */
data class WorkExperience(

  @field:Valid
  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("typeOfWorkExperience", required = true)
  val typeOfWorkExperience: WorkType,

  @Schema(
    example = "null",
    description = "A work experience, which is not listed in 'typeOfWorkExperience' Enum. Mandatory when 'typeOfWorkExperience' includes 'OTHER'.",
  )
  @get:JsonProperty("otherWork")
  val otherWork: String? = null,

  @Schema(example = "null", description = "This is the role the Prisoner had.")
  @get:JsonProperty("role")
  val role: String? = null,

  @Schema(example = "null", description = "Additional details of the work.")
  @get:JsonProperty("details")
  val details: String? = null,
)
