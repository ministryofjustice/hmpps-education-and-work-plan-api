package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

/**
 * A Prisoner's in-prison work and education interests. This is a temporary class to allow us to import data from the CIAG API.
 */
data class PrisonWorkAndEducationResponse(

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who last updated this Prisoner's in-prison work and education interests.",
  )
  @get:JsonProperty("modifiedBy", required = true)
  val modifiedBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when this Prisoner's in-prison work and education interests was last updated. This will be the same as the created date if it has not yet been updated.",
  )
  @get:JsonProperty("modifiedDateTime", required = true)
  val modifiedDateTime: java.time.OffsetDateTime,

  @field:Valid
  @get:Size(min = 1)
  @Schema(example = "null", description = "A list of in-prison work that the Prisoner is interested in.")
  @get:JsonProperty("inPrisonWork")
  val inPrisonWork: Set<InPrisonWorkType>? = null,

  @Schema(
    example = "null",
    description = "A specific type of in-prison work that does not fit the given 'inPrisonWork' types. Mandatory when 'inPrisonWork' includes 'OTHER'.",
  )
  @get:JsonProperty("inPrisonWorkOther")
  val inPrisonWorkOther: String? = null,

  @field:Valid
  @get:Size(min = 1)
  @Schema(
    example = "null",
    description = "Any potential in-prison education/training that the Prisoner is interested in.",
  )
  @get:JsonProperty("inPrisonEducation")
  val inPrisonEducation: Set<InPrisonTrainingType>? = null,

  @Schema(
    example = "null",
    description = "A specific type of in-prison education/training that does not fit the given 'inPrisonEducation' types. Mandatory when 'inPrisonEducation' includes 'OTHER'.",
  )
  @get:JsonProperty("inPrisonEducationOther")
  val inPrisonEducationOther: String? = null,
)
