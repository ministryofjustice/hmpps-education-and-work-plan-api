package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * Detail about a Prisoner's work interest.
 */
data class WorkInterestDetail(

  @field:Valid
  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("workInterest", required = true)
  val workInterest: WorkType,

  @Schema(example = "null", description = "The role within a Prisoner's area of work interest.")
  @get:JsonProperty("role")
  val role: String? = null,
)
