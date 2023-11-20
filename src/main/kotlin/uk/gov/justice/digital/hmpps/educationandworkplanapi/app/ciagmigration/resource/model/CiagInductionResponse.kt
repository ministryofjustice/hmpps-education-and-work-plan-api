package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

/**
 * A Prisoner's CIAG Induction containing information, such as their education history and future work interests.
 * This is a temporary class to enable us to import data from the CIAG API.
 */
data class CiagInductionResponse(

  @get:Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$")
  @Schema(example = "null", required = true, description = "The ID of the Prisoner. AKA the prison number.")
  @get:JsonProperty("offenderId", required = true)
  val offenderId: String,

  @field:Valid
  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hopingToGetWork", required = true)
  val hopingToGetWork: HopingToWork,

  @Schema(
    example = "asmith_gen",
    required = true,
    description = "The DPS username of the person who created the Induction.",
  )
  @get:JsonProperty("createdBy", required = true)
  val createdBy: String,

  @Schema(
    example = "2023-06-19T09:39:44Z",
    required = true,
    description = "An ISO-8601 timestamp representing when the Induction was created.",
  )
  @get:JsonProperty("createdDateTime", required = true)
  val createdDateTime: java.time.OffsetDateTime,

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

  @Schema(example = "null", description = "The ID of the Prison that that Prisoner is currently at.")
  @get:JsonProperty("prisonId")
  val prisonId: String? = null,

  @Schema(example = "null", description = "Whether the Prisoner wishes to work or not.")
  @get:JsonProperty("desireToWork")
  val desireToWork: Boolean? = null,

  @Schema(
    example = "null",
    description = "The reason that is given when the Prisoner does not want to work. This is mandatory when 'reasonToNotGetWork' is set to 'OTHER'.",
  )
  @get:JsonProperty("reasonToNotGetWorkOther")
  val reasonToNotGetWorkOther: String? = null,

  @field:Valid
  @Schema(example = "null", description = "One or more factors affecting the Prisoner's ability to work.")
  @get:JsonProperty("abilityToWork")
  val abilityToWork: Set<AbilityToWorkFactor>? = null,

  @Schema(
    example = "null",
    description = "A specific factor affecting the Prisoner's ability to work. This is mandatory when 'abilityToWork' is set to 'OTHER'.",
  )
  @get:JsonProperty("abilityToWorkOther")
  val abilityToWorkOther: String? = null,

  @field:Valid
  @Schema(
    example = "null",
    description = "One or more reasons the Prisoner has given not to get work after leaving Prison.",
  )
  @get:JsonProperty("reasonToNotGetWork")
  val reasonToNotGetWork: Set<ReasonNotToWork>? = null,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("workExperience")
  val workExperience: PreviousWorkResponse? = null,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("skillsAndInterests")
  val skillsAndInterests: SkillsAndInterestsResponse? = null,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("qualificationsAndTraining")
  val qualificationsAndTraining: EducationAndQualificationResponse? = null,

  @field:Valid
  @Schema(example = "null", description = "")
  @get:JsonProperty("inPrisonInterests")
  val inPrisonInterests: PrisonWorkAndEducationResponse? = null,
)
