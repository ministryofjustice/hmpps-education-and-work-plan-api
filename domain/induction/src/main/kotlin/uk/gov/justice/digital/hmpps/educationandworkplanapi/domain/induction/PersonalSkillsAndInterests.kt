package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

/**
 * Lists the personal skills (such as communication) and interests (such as music) that a Prisoner feels they have.
 *
 * Note that the lists of skills/interests cannot be empty, since NONE is an option in both cases.
 */
data class PersonalSkillsAndInterests(
  val reference: UUID,
  val skills: List<PersonalSkill>,
  val interests: List<PersonalInterest>,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedByDisplayName: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
)

data class PersonalSkill(
  val skillType: SkillType,
  val skillTypeOther: String?,
)

data class PersonalInterest(
  val interestType: InterestType,
  val interestTypeOther: String?,
)

enum class SkillType {
  COMMUNICATION,
  POSITIVE_ATTITUDE,
  RESILIENCE,
  SELF_MANAGEMENT,
  TEAMWORK,
  THINKING_AND_PROBLEM_SOLVING,
  WILLINGNESS_TO_LEARN,
  OTHER,
  NONE,
}

enum class InterestType {
  COMMUNITY,
  CRAFTS,
  CREATIVE,
  DIGITAL,
  KNOWLEDGE_BASED,
  MUSICAL,
  OUTDOOR,
  NATURE_AND_ANIMALS,
  SOCIAL,
  SOLO_ACTIVITIES,
  SOLO_SPORTS,
  TEAM_SPORTS,
  WELLNESS,
  OTHER,
  NONE,
}
