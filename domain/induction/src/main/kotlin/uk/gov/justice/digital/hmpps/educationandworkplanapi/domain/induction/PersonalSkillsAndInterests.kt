package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

/**
 * Lists the personal skills (such as communication) and interests (such as music) that a Prisoner feels they have.
 */
data class PersonalSkillsAndInterests(
  val skills: List<PersonalSkill>,
  val interests: List<PersonalInterest>,
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
