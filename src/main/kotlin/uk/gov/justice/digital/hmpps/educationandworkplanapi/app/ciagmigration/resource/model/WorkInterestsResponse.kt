package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

/**
 * The Prisoner's work interests. This is a temporary class to allow us to import data from the CIAG API.
 */
data class WorkInterestsResponse(

  @Schema(
    example = "c88a6c48-97e2-4c04-93b5-98619966447b",
    required = true,
    description = "A unique reference for this Prisoner's work interests.",
  )
  @get:JsonProperty("id", required = true)
  val id: java.util.UUID,

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who last updated the Induction.",
  )
  @get:JsonProperty("modifiedBy", required = true)
  val modifiedBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when the Goal was last updated. This will be the same as the created date if it has not yet been updated.",
  )
  @get:JsonProperty("modifiedDateTime", required = true)
  val modifiedDateTime: java.time.OffsetDateTime,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", required = true, description = "A list of Prisoner's future work interests.")
  @get:JsonProperty("workInterests", required = true)
  val workInterests: Set<WorkType>,

  @Schema(
    example = "null",
    description = "A work interest, which is not listed in 'workInterests' Enum. Mandatory when 'workInterests' includes 'OTHER'.",
  )
  @get:JsonProperty("workInterestsOther")
  val workInterestsOther: String? = null,

  @field:Valid
  @Schema(example = "null", description = "A detailed list of work interests that a Prisoner has.")
  @get:JsonProperty("particularJobInterests")
  val particularJobInterests: Set<WorkInterestDetail>? = null,
)
